package com.library.base.config

import com.blankj.utilcode.util.SPUtils


object GlobalConfig {
    var tipVibrate = true
    var tipRing = true
    var lowBatteryUpload = true
    var ChargingUpload = false


    fun initConfig() {
        tipVibrate = SPUtils.getInstance().getBoolean(SpConstant.tipVibrate, true)
        tipRing = SPUtils.getInstance().getBoolean(SpConstant.tipRing, true)
        lowBatteryUpload = SPUtils.getInstance().getBoolean(SpConstant.lowBatteryUpload, true)
    }

    fun saveTipVibrate(status: Boolean) {
        SPUtils.getInstance().put(SpConstant.tipVibrate, status)
        tipVibrate = status
    }

    fun saveTipRing(status: Boolean) {
        SPUtils.getInstance().put(SpConstant.tipRing, status)
        tipRing = status
    }

    fun saveLowBatteryUpload(status: Boolean) {
        SPUtils.getInstance().put(SpConstant.lowBatteryUpload, status)
        lowBatteryUpload = status
    }


}