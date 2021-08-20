package com.library.base.config

import android.text.TextUtils
import com.blankj.utilcode.util.GsonUtils
import com.library.base.utils.KV

object GlobalUser {
    var token: String? = null
    var userinfo: UserBean? = null

    fun initData() {
        token = KV.getStr(SpConstant.TokenKey)
        val userstr = KV.getStr(SpConstant.UserInfo)
        userinfo = GsonUtils.fromJson(userstr, UserBean::class.java)
    }

    fun saveToken(token: String) {
        if (token.isNotEmpty()) {
            KV.saveStr(SpConstant.TokenKey, token)
            this.token = token
        }
    }
    fun isLogin():Boolean {
        if(TextUtils.isEmpty(token)){
            return false
        }
        return true;
    }

    fun saveUserInfo(userBean: UserBean) {
        val toJson = GsonUtils.toJson(userBean)
        KV.saveStr(SpConstant.UserInfo, toJson)
        this.userinfo = userBean
    }

    fun updateUserName(username: String) {
        this.userinfo?.user_name = username
        saveUserInfo(this.userinfo!!)
    }

    fun clearToken() {
        KV.removeValue(SpConstant.TokenKey)
        KV.removeValue(SpConstant.UserInfo)
        this.token = null
        this.userinfo = null
    }
}