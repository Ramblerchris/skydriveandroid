package com.library.base.utils

import com.blankj.utilcode.util.VibrateUtils
import com.library.R
import com.library.base.config.GlobalConfig

/**
 *
 * @Description:
 * @Author: Wisn
 * @CreateDate: 2021/2/1 上午11:16
 */
object UploadTip {

    fun tipVibrate(milliseconds:Long=30) {
        if (GlobalConfig.tipVibrate) {
            VibrateUtils.vibrate(milliseconds)
        }
    }

    fun tipRing() {
        if (GlobalConfig.tipRing) {
            SoundPoolUtils.getInstance().play(R.raw.device)
        }
    }
}