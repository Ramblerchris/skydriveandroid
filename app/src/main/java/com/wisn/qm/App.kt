package com.wisn.qm

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.Utils
import com.library.base.BaseApp
import com.library.base.config.GlobalUser
import com.squareup.leakcanary.RefWatcher
import com.tencent.bugly.crashreport.CrashReport
import com.wisn.qm.mode.beans.FileType
import com.wisn.qm.mode.db.AppDataBase
import com.wisn.qm.task.TaskUitls
import com.wisn.qm.ui.SplashActivity
import io.github.skyhacker2.sqliteonweb.SQLiteOnWeb
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

open class App : BaseApp() {

    override fun onCreate() {
        super.onCreate()
        CrashReport.initCrashReport(getApplicationContext(), "553ef3762f", false);
        SQLiteOnWeb.init(this).start() //webSqlite
    }
    companion object {
        fun getInstance():App{
            return app as App
        }
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
    var isResetSuccess = false;
    override fun initTodoBeforNetAvailable() {
        super.initTodoBeforNetAvailable()
        GlobalScope.launch {
            try {
                //重置上次上传一半的任务，再次重试上传
                AppDataBase.getInstanse().uploadBeanDao?.updateUploadBeanStatusByStatus(
                    FileType.UPloadStatus_uploading,
                    FileType.UPloadStatus_Noupload
                )
                isResetSuccess = true
            } catch (e: Exception) {
            }
        }
    }

    override fun netAvailabble() {
        super.netAvailabble()
        if (isResetSuccess) {
            TaskUitls.exeUploadRequest(Utils.getApp(), TaskUitls.buildUploadRequest())
        }
    }
}