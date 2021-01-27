package com.library.base.utils

import java.text.SimpleDateFormat
import java.util.*

object FormatStrUtils {

    private const val DF_HH_MM_SS = "HH:mm:ss"

    private const val minute = 60 * 1000.toLong() // 1分钟

    private const val hour = 60 * minute // 1小时

    private const val day = 24 * hour // 1天

    private const val month = 31 * day // 月

    private const val year = 12 * month // 年

    private const val KB = 1024
    private const val MB = 1024 * KB
    private const val GB = 1024 * MB
    private const val TB = 1024 * GB



    /**
     * 将日期格式化成友好的字符串：几分钟前、几小时前、几天前、几月前、几年前、刚刚
     *
     * @param mDate
     * @return
     */
    fun getformatDate(mDate: Long): String? {
        val date = Date(mDate)
        val diff = System.currentTimeMillis() - date.time
        var r: Long = 0
        if (diff > year) {
            r = diff / year
            return r.toString() + "年前"
        }
        if (diff > month) {
            r = diff / month
            return r.toString() + "个月前"
        }
        if (diff > day) {
            r = diff / day
            return if (r == 1L) {
                "昨天" + SimpleDateFormat(DF_HH_MM_SS).format(Date(mDate))
            } else r.toString() + "天前"
        }
        if (diff > hour) {
            r = diff / hour
            return r.toString() + "小时前"
        }
        if (diff > minute) {
            r = diff / minute
            return r.toString() + "分钟前"
        }
        return "刚刚"
    }


    /**
     * 时：分：秒/分：秒
     */
    fun getFormatTimeStr(duration: Long): String {
//        var seconds = Math.floor(duration / 1000.toDouble()).toInt()
        var seconds = duration
        var hours = 0L
        var minutes = 0L
        if (seconds >= hour) {
            hours = seconds / hour
            seconds -= hours * hour
        }
        if (seconds >= minute) {
            minutes = seconds / minute
            seconds -= minutes * minute
        }
        seconds /= 1000
        if (hours > 0) {
            return String.format("%2d:%02d:%02d", hours, minutes, seconds)
        } else {
            return String.format("%02d:%02d", minutes, seconds)
        }
    }


    fun getFormatDiskSizeStr(limit: Long): String {
        var size: String
        if (limit < 0.1 * KB) {
            // 如果小于0.1KB转化成B
            size = String.format("%.2fB", limit.toFloat())
        } else if (limit < 0.1 * MB) {
            // 如果小于0.1MB转化成KB
            size = String.format("%.2fKB", limit.toFloat() / KB)
        } else if (limit < 0.1 * GB) {
            // 如果小于0.1GB转化成MB
            size = String.format("%.2fMB", limit.toFloat() / MB)
        } else {
            // 其他转化成GB
            size = String.format("%.2fGB", limit.toFloat() / GB)
        }
        return size
    }


}