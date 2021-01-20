package com.library.base.config
import com.blankj.utilcode.util.SPUtils


object Constant {
    const val TAGBASE_URL = "BASE_URL"
    const val TokenKey = "Token"
    const val id = "Id"
    const val TypeDir = 1
    const val TypeFile = -1
    const val UserInfo = "user"
    var BASE_URL = "http://192.168.0.100:9996/"









    fun getImageUrl(sha1: String?): String {
        if (sha1.isNullOrEmpty()) {
            return "";
        }
        return "${BASE_URL}file/open?filesha1=${sha1}&token=${GlobalUser.token}"
    }

    fun getImageUrlThumb(sha1: String?): String {
        if (sha1.isNullOrEmpty()) {
            return "";
        }
        return "${BASE_URL}file/open?filesha1=${sha1}&token=${GlobalUser.token}&q=5"
    }

    fun getImageDownloadUrl(sha1: String): String {
        return "${BASE_URL}file/getdownload?filesha1=${sha1}&token=${GlobalUser.token}"
    }


    fun initBaseUrl() {
        BASE_URL = SPUtils.getInstance().getString(TAGBASE_URL)
    }


    fun setBaseUrl(ip: String): String {
        ip?.let {
            BASE_URL = getBaseTemp(ip)
            SPUtils.getInstance().put(TAGBASE_URL, BASE_URL)
        }
        return BASE_URL
    }

    fun getBaseTemp(ip: String): String {
        return "http://$ip:9996/";
    }
}