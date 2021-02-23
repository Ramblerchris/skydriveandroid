package com.wisn.qm

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.Utils
import com.library.base.BaseApp
import com.library.base.config.GlobalUser
import com.tencent.bugly.crashreport.CrashReport
import com.wisn.qm.task.TaskUitls
import com.wisn.qm.ui.SplashActivity
import io.github.skyhacker2.sqliteonweb.SQLiteOnWeb

open class App : BaseApp() {

    override fun onCreate() {
        super.onCreate()
        CrashReport.initCrashReport(getApplicationContext(), "553ef3762f", false);
        SQLiteOnWeb.init(this).start() //webSqlite
    }

    override fun loginEvent() {
        GlobalUser.clearToken()
        ActivityUtils.finishAllActivities()
        LogUtils.d("App", "loginEvent")
//       val intentOf = QMUIFragmentActivity.intentOf(this, MainActivity::class.java, LoginFragment::class.java)
        val intentOf = Intent(this, SplashActivity::class.java)
        intentOf.flags = FLAG_ACTIVITY_NEW_TASK
        startActivity(intentOf)
       /* val topActivity = ActivityUtils.getTopActivity()
        if (topActivity is MainActivity) {
            topActivity.startFragmentAndDestroyCurrent(LoginFragment(), false)
        } else {
            ActivityUtils.finishAllActivities()
            LogUtils.d("App", "loginEvent")
//       val intentOf = QMUIFragmentActivity.intentOf(this, MainActivity::class.java, LoginFragment::class.java)
            val intentOf = Intent(this, SplashActivity::class.java)
            intentOf.flags = FLAG_ACTIVITY_NEW_TASK
            startActivity(intentOf)
        }*/
    }

    override fun netAvailabble() {
        super.netAvailabble()
        TaskUitls.exeRequest(Utils.getApp(), TaskUitls.buildUploadRequest())
    }
}