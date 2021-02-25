package com.library.base.config

import com.blankj.utilcode.util.GsonUtils
import com.library.base.utils.KV

object GlobalUser {
    var token: String? = null
    var userinfo: UserBean? = null

    fun initData() {
//        token = SPUtils.getInstance().getString(SpConstant.TokenKey)
        token = KV.getStr(SpConstant.TokenKey)
//        val userstr = SPUtils.getInstance().getString(SpConstant.UserInfo)
        val userstr = KV.getStr(SpConstant.UserInfo)
        userinfo = GsonUtils.fromJson(userstr, UserBean::class.java)
    }

    fun saveToken(token: String) {
        if (token.isNotEmpty()) {
//            SPUtils.getInstance().put(SpConstant.TokenKey, token)
            KV.saveStr(SpConstant.TokenKey, token)
            this.token = token
        }
    }

    fun saveUserInfo(userBean: UserBean) {
        val toJson = GsonUtils.toJson(userBean)
//        SPUtils.getInstance().put(SpConstant.UserInfo, toJson)
        KV.saveStr(SpConstant.UserInfo, toJson)
        this.userinfo = userBean
    }

    fun updateUserName(username: String) {
        this.userinfo?.user_name = username
        saveUserInfo(this.userinfo!!)
    }

    fun clearToken() {
//        SPUtils.getInstance().put(SpConstant.TokenKey, "")
        KV.removeValue(SpConstant.TokenKey)
//        SPUtils.getInstance().put(SpConstant.UserInfo, "")
        KV.removeValue(SpConstant.UserInfo)
        this.token = null
        this.userinfo = null
    }
}