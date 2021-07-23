package com.wisn.qm.ui.preview


/**
 *
 * @Description:
 * @Author: Wisn
 * @CreateDate: 2020/11/26 下午3:41
 */
object CacheUrl {
    var map=HashMap<String,String>();
    fun getOriginUrl(originUrl:String):String?{
        return  map.get(originUrl);
    }
    fun addOriginUrl(originUrl:String,resultUrl:String):String?{
        return  map.put(originUrl,resultUrl);
    }

}