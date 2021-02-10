package com.wisn.qm.ui.user

import androidx.lifecycle.MutableLiveData
import com.library.base.BaseApp
import com.library.base.base.BaseViewModel
import com.library.base.config.GlobalUser
import com.library.base.event.Message
import com.wisn.qm.mode.net.ApiNetWork

class UserViewModel : BaseViewModel() {

    private var login = MutableLiveData<String>()
    private var register = MutableLiveData<String>()

    fun login(phone: String, password: String): MutableLiveData<String> {
        launchGo({
            val loginresult = ApiNetWork.newInstance().login(phone, password);
            if (loginresult.isSuccess()) {
                GlobalUser.saveToken(loginresult.data)
                ApiNetWork.newInstance().login(phone, password);
                val userInfo = ApiNetWork.newInstance().getUserInfo()
                if (userInfo.isSuccess()) {
                    GlobalUser.saveUserInfo(userInfo.data)
                    defUi.msgEvent.value = Message(100)
                }
            }
            login.value = loginresult.msg()
            loginresult
        })
        return login
    }

    fun singout() {
        launchGo({
            val singout = ApiNetWork.newInstance().singout()
            singout
        }, complete = {
            GlobalUser.clearToken()
            BaseApp.app.loginEvent()
        })
    }


    fun register(phone: String, password: String, password1: String): MutableLiveData<String> {
        launchGo({
            val registerResult = ApiNetWork.newInstance().register(phone, password, password1);
            if (registerResult.isSuccess()) {
                defUi.msgEvent.value = Message(100)
            }
            register.value = registerResult.msg()
            registerResult
        })
        return register
    }


    fun cancelAdminSDRB() {
        adminSDRB("sd_cancel",null,"清除所有任务")
    }

    fun adminSDRB(type: String, time: String?,tipmessage:String?="") {
        launchGo({
            val adminSDRB = ApiNetWork.newInstance().adminSDRB(type, time);
            if (!adminSDRB.isSuccess()) {
                defUi.toastEvent.value = adminSDRB.msg()
            }
            defUi.msgEvent.value = Message(100,"${tipmessage} 结果： ${adminSDRB.isSuccess()} Message:${adminSDRB.msg()}")
            adminSDRB
        })
    }
}