package com.library.base.utils

import com.blankj.utilcode.util.VibrateUtils
import com.library.base.config.GlobalConfig

/**
 *
 * @Description:
 * @Author: Wisn
 * @CreateDate: 2021/2/1 上午11:16
 */
object UploadTip {

    fun tipVibrate() {
        if (GlobalConfig.tipVibrate) {
            VibrateUtils.vibrate(30)
        }
    }

    fun tipRing() {
        if (GlobalConfig.tipRing) {

        }
    }
}