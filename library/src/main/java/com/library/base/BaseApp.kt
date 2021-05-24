package com.library.base
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Build
import androidx.multidex.MultiDexApplication
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.Utils
import com.coder.zzq.smartshow.core.SmartShow
import com.library.base.config.Constant
import com.library.base.config.GlobalConfig
import com.library.base.config.GlobalUser
import com.qmuiteam.qmui.arch.QMUISwipeBackActivityManager
import com.squareup.leakcanary.LeakCanary
import com.squareup.leakcanary.RefWatcher
import com.tencent.mmkv.MMKV

open class BaseApp : MultiDexApplication() {

    companion object {
         lateinit var app: BaseApp
         lateinit var refwatcher: RefWatcher
    }

    override fun onCreate() {
        super.onCreate()
        if (!LeakCanary.isInAnalyzerProcess(this)) {
            refwatcher= LeakCanary.install(this)
        }
        netWorkChangeListener()
    }
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        app = this
        Utils.init(this)
        val rootDir: String = MMKV.initialize(this)
        LogUtils.d("BaseApp",rootDir)
        QMUISwipeBackActivityManager.init(this)
        GlobalConfig.initConfig()
        Constant.initBaseUrl()
        GlobalUser.initData()
        SmartShow.init(this)
    }
    open fun loginEvent(){
    }
    open fun netWorkChangeListener() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
                // 请注意这里会有一个版本适配bug，所以请在这里添加非空判断
                connectivityManager?.requestNetwork(NetworkRequest.Builder().build(), object : ConnectivityManager.NetworkCallback() {
                    /**
                     * 网络可用的回调
                     */
                    override fun onAvailable(network: Network) {
                        super.onAvailable(network)
//                        isNetConnected = true
//                        EventBus.getDefault().post(NetWorkStateChangeEvent(NetWorkStateChangeEvent.NetWorkAvailable))
                        netAvailabble()
                    }

                    /**
                     * 网络丢失的回调
                     */
                    override fun onLost(network: Network) {
                        super.onLost(network)
//                        isNetConnected = false
//                        EventBus.getDefault().post(NetWorkStateChangeEvent(NetWorkStateChangeEvent.NetWorkLoss))
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            val netWorkStateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
//                    isNetConnected = Utils.getApp().isNetConnected
//                    EventBus.getDefault().post(NetWorkStateChangeEvent(if (isNetConnected) NetWorkStateChangeEvent.NetWorkAvailable else NetWorkStateChangeEvent.NetWorkLoss))
                    netAvailabble()
                }
            }
            val filter = IntentFilter()
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
            registerReceiver(netWorkStateReceiver, filter)
        }
    }
    open fun netAvailabble(){
    }
}