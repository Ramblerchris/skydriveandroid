package com.wisn.qm.ui.preview


/**
 *
 * @Description:
 * @Author: Wisn
 * @CreateDate: 2020/11/26 下午3:41
 */
object CacheUrl {
    var loadOriginMap = HashMap<String, String>();
    var downloadMap = HashMap<String, String>();
    fun getOriginUrl(originUrl: String): String? {
        return loadOriginMap.get(originUrl);
    }

    fun getDownloadUrl(originUrl: String): String? {
        return downloadMap.get(originUrl);
    }

    fun addOriginUrl(originUrl: String, resultUrl: String) {
        loadOriginMap.put(originUrl, resultUrl);
    }

    fun addDownloadUrl(originUrl: String, resultUrl: String) {
        downloadMap.put(originUrl, resultUrl);
    }


}