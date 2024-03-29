package com.library.base.utils

import android.content.Context

object PhoneUtils{
    fun getPhoneWid(context: Context): Int {
       /* val windowManager = context.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metric = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metric)
        return metric.widthPixels*/
        return  context.applicationContext.resources.displayMetrics.widthPixels
    }

    fun getPhoneHei(context: Context): Int {
      /*  val windowManager = context.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metric = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metric)
        return metric.heightPixels*/
        return context.applicationContext.resources.displayMetrics.heightPixels
    }

    fun getPhoneRatio(context: Context): Float {
        return getPhoneHei(context).toFloat() / getPhoneWid(context).toFloat()
    }

    fun px2dp(context: Context, pxValue: Float): Int {
        val scale = context.applicationContext.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }

    fun dp2px(context: Context, dipValue: Float): Int {
        val scale = context.applicationContext.resources.displayMetrics.density
        return (dipValue * scale + 0.5f).toInt()
    }

}