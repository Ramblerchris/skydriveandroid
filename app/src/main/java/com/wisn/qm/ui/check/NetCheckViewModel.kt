package com.wisn.qm.ui.check

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.LogUtils
import com.library.base.BaseApp
import com.library.base.base.BaseViewModel
import com.library.base.net.broadcast.BroadcastAll
import com.library.base.net.broadcast.BroadcastGroup
import com.library.base.net.broadcast.MessageCall
import com.library.base.net.broadcast.UdpConfig
import com.library.base.utils.NetCheckUtils
import com.wisn.qm.BuildConfig
import com.wisn.qm.mode.beans.ScanMessage
import com.wisn.qm.mode.net.ApiNetWork
import java.util.*
import kotlin.concurrent.fixedRateTimer

class NetCheckViewModel : BaseViewModel() {
    private var result: MutableLiveData<String?>? = null
    private var fixedRateTimer: Timer? = null
    private var ipTemp: String? = null
    private val TAG = "CheckNetViewModel"
    var isSuccess: MutableLiveData<Boolean> = MutableLiveData()
    var broadCastGroup = BroadcastGroup.getInstance()
    var broadCastAll = BroadcastAll.getInstance()


    fun initBroadcastListener() {
        try {
            broadCastGroup?.listenerMessage(UdpConfig.groupIp, UdpConfig.Group_ClientListenport, MessageCall { message, ip ->
                setResult("group $message ",message, ip)
            })
            broadCastAll?.listenerMessage(UdpConfig.All_ClientListenport, MessageCall { message, ip ->
                setResult("broadcast $message ",message, ip)
            })
            //            WifiManager wifiManager = (WifiManager) App.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            val wifiManager = BaseApp.app.getApplicationContext().getSystemService(Context.WIFI_SERVICE) as WifiManager
            fixedRateTimer = fixedRateTimer("", false, 100, 2000) {
                var multicastLock = wifiManager.createMulticastLock("multicast.test")
                multicastLock!!.acquire()
                broadCastGroup!!.sendBackMessage(UdpConfig.groupIp, UdpConfig.Group_ServerportRecevie, "group broadcast request ping")
                LogUtils.d(TAG, "发送 探针 ${Thread.currentThread().name}")
                getResult().postValue("${System.currentTimeMillis()} 发送 探针...")
                broadCastAll!!.sendBackMessage(UdpConfig.groupALL, UdpConfig.All_ServerportRecevie, "broadcast request ping")
                multicastLock.release();
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun setResult(message: String?,data:String?, ip: String) {
        LogUtils.d(TAG, " MESSAGE:$message ip:$ip")
        val fromJson = GsonUtils.fromJson<ScanMessage>(data, ScanMessage::class.java)
        if (fromJson.debug == BuildConfig.DEBUG) {
            val handler = Handler(Looper.getMainLooper())
            handler.post {
                getResult().setValue("ip:$ip $message  ")
                setServerIp(ip)
            }
        }
    }

    fun getResult(): MutableLiveData<String?> {
        if (result == null) {
            result = MutableLiveData<String?>()
        }
        return result!!
    }


    fun setServerIp(ip: String) {
        if (ipTemp.isNullOrEmpty()) {
            ipTemp = ip
            getResult().setValue(" 开始连接 ip:$ip ")
            launchGoLo({
                LogUtils.d(TAG, Thread.currentThread().name)
                if (NetCheckUtils.isConnectByIp(ip)) {
                    getResult().setValue("服务器 ip:$ip 连接成功")
                    ApiNetWork.newInstance().updateBaseUrl(ip)
                    fixedRateTimer?.cancel()
                    LogUtils.d(TAG, Thread.currentThread().name)
                    isSuccess.postValue(true)
                } else {
                    ipTemp = null
                    getResult().setValue("服务器 ip:$ip 连接失败")
                }
            })
        }

    }

    fun destory() {
        fixedRateTimer?.cancel()
        broadCastGroup?.cancel()
        broadCastAll?.cancel()

    }

}