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
import com.library.base.utils.NetCheckUtils
import com.qmuiteam.qmui.arch.QMUISwipeBackActivityManager
import com.squareup.leakcanary.LeakCanary
import com.squareup.leakcanary.RefWatcher
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

open class BaseApp : MultiDexApplication() {
    var isNetConnect: Boolean = true

    companion object {
        lateinit var app: BaseApp
        lateinit var refwatcher: RefWatcher
    }

    override fun onCreate() {
        super.onCreate()
        if (!LeakCanary.isInAnalyzerProcess(this)) {
            refwatcher = LeakCanary.install(this)
        }
        initTodoBeforNetAvailable()
        netWorkChangeListener()
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        app = this
        Utils.init(this)
        val rootDir: String = MMKV.initialize(this)
        LogUtils.d("BaseApp", rootDir)
        QMUISwipeBackActivityManager.init(this)
        GlobalConfig.initConfig()
        Constant.initBaseUrl()
        GlobalUser.initData()
        SmartShow.init(this)
    }

    open fun loginEvent() {
    }

    open fun netWorkChangeListener() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                val connectivityManager =
                    getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
                // 请注意这里会有一个版本适配bug，所以请在这里添加非空判断
                connectivityManager.requestNetwork(
                    NetworkRequest.Builder().build(),
                    object : ConnectivityManager.NetworkCallback() {
                        /**
                         * 网络可用的回调
                         */
                        override fun onAvailable(network: Network) {
                            super.onAvailable(network)
                            GlobalScope.launch {
                                val connectCheckInit = NetCheckUtils.isConnectCheckInit()
                                if (connectCheckInit) {
                                    isNetConnect = true
                                    withContext(Dispatchers.Main) {
                                        netAvailabble()
                                    }
                                }
                            }
                        }

                        /**
                         * 网络丢失的回调
                         */
                        override fun onLost(network: Network) {
                            super.onLost(network)
                            isNetConnect = false
                        }
                    })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            val netWorkStateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    GlobalScope.launch {
                        val connectCheckInit = NetCheckUtils.isConnectCheckInit()
                        if (connectCheckInit) {
                            isNetConnect = true
                            withContext(Dispatchers.Main) {
                                netAvailabble()
                            }
                        }
                    }
                }
            }
            val filter = IntentFilter()
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
            registerReceiver(netWorkStateReceiver, filter)
        }
    }

    open fun initTodoBeforNetAvailable() {
    }

    open fun netAvailabble() {
    }
}