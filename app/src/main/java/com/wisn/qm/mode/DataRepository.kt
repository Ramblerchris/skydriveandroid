package com.wisn.qm.mode

import com.wisn.qm.mode.db.AppDataBase
import com.wisn.qm.mode.db.beans.Folder
import com.wisn.qm.mode.db.beans.MediaInfo
import com.wisn.qm.mode.db.beans.UserDirBean
import com.wisn.qm.mode.file.MediaInfoScanHelper
import com.wisn.qm.mode.net.ApiNetWork
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.lang.Exception

class DataRepository private constructor(val apiNetWork: ApiNetWork, val appDataBase: AppDataBase, val mediaInfohelper: MediaInfoScanHelper) {


    suspend fun getMediaImageList(maxid: String): MutableList<MediaInfo> {
        return mediaInfohelper.getMediaImageList(maxid)
    }

    suspend fun getMediaImageAndVideoList(maxid: String): MutableList<MediaInfo>? {
       /* val mediaImageList = mediaInfohelper.getMediaImageList(maxid)
        val mediaVideoList = mediaInfohelper.getMediaVideoList(maxid)
        if (mediaImageList.isNotEmpty()) {
            mediaImageList.addAll(mediaVideoList)
            return mediaImageList
        } else if (mediaVideoList.isNotEmpty()) {
            mediaVideoList.addAll(mediaImageList)
            return mediaVideoList
        }*/
        //async是不阻塞线程的
        val mediaImageList =GlobalScope.async { mediaInfohelper.getMediaImageList(maxid) }
        val mediaVideoList = GlobalScope.async {mediaInfohelper.getMediaVideoList(maxid)}
        val await = mediaImageList.await();
        await.addAll(mediaVideoList.await())
        return await
    }

    suspend fun getMediaImageAndVideoListNoSha1(addVideo: Boolean): ArrayList<Folder> {
        return mediaInfohelper.getMediaImageVidoeListNoSha1(addVideo)
    }

    suspend fun getUserDirlist(isUserCache: Boolean): MutableList<UserDirBean>? {
        try {
            val dirlist = apiNetWork.getUserDirlist(-1,pageSize = -1)
            if (dirlist.isSuccess()) {
                return dirlist.data.list.also {
                    if (isUserCache) {
                        appDataBase.userDirDao?.deleteAllDirBeanList()
                        appDataBase.userDirDao?.insertUserDirBeanList(it)
                    }
                }
            }
        } catch (e: Exception) {

        }
        return appDataBase.userDirDao?.getAllUserDirBeanList()
    }


    suspend fun getMediaVideoList(maxid: String): MutableList<MediaInfo> {
        return mediaInfohelper.getMediaVideoList(maxid)
    }


    companion object {
        private var INSTANCE: DataRepository? = null
        fun getInstance(): DataRepository =
                INSTANCE ?: synchronized(this) {
                    INSTANCE
                            ?: DataRepository(ApiNetWork.newInstance(), AppDataBase.getInstanse(), MediaInfoScanHelper.newInstance()).also { INSTANCE = it }
                }

    }
}