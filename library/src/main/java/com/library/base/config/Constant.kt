package com.library.base.config
import com.blankj.utilcode.util.LogUtils
import com.library.base.utils.KV


object Constant {
    const val TypeDir = 1
    const val Timeout:Long=2
    const val HttpTAG:String="SkyDriveNet"
    const val CONNECT_TIMEOUT = "CONNECT_TIMEOUT"
    const val READ_TIMEOUT = "READ_TIMEOUT"
    const val WRITE_TIMEOUT = "WRITE_TIMEOUT"
    const val TypeFile = -1
    var BASE_URL = ""

    fun getImageUrl(sha1: String?): String {
        if (sha1.isNullOrEmpty()) {
            return ""
        }
        return "${BASE_URL}file/openV2?filesha1=${sha1}&token=${GlobalUser.token}"
    }

    fun getImageUrlThumb(sha1: String?): String {
        if (sha1.isNullOrEmpty()) {
            return "";
        }
        return "${BASE_URL}file/openV2?filesha1=${sha1}&token=${GlobalUser.token}&q=3&widthf=0.3&width=0"
    }


    fun getImageDownloadUrl(sha1: String): String {
        return "${BASE_URL}file/getdownload?filesha1=${sha1}&token=${GlobalUser.token}"
    }


    fun initBaseUrl() {
        BASE_URL = KV.getStr(SpConstant.TAGBASE_URL)
        LogUtils.d("BASE_URL",BASE_URL)

    }


    fun setBaseUrl(ip: String): String {
        ip?.let {
            BASE_URL = getBaseTemp(ip)
//            SPUtils.getInstance().put(SpConstant.TAGBASE_URL, BASE_URL)
           KV.saveStr(SpConstant.TAGBASE_URL, BASE_URL)
        }
        LogUtils.d("BASE_URL",BASE_URL)
        return BASE_URL
    }

    fun getBaseTemp(ip: String): String {
        return "http://$ip:9996/";
    }
}