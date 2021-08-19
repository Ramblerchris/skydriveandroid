package com.wisn.qm.mode

import com.blankj.utilcode.util.LogUtils
import com.wisn.qm.mode.db.AppDataBase
import com.wisn.qm.mode.db.beans.Folder
import com.wisn.qm.mode.db.beans.MediaInfo
import com.wisn.qm.mode.db.beans.UserDirBean
import com.wisn.qm.mode.file.MediaInfoScanHelper
import com.wisn.qm.mode.net.ApiNetWork
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.io.File
import java.lang.Exception

class DataRepository private constructor(
    val apiNetWork: ApiNetWork,
    val appDataBase: AppDataBase,
    val mediaInfohelper: MediaInfoScanHelper
) {


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
        val mediaImageList = GlobalScope.async { mediaInfohelper.getMediaImageList(maxid) }
        val mediaVideoList = GlobalScope.async { mediaInfohelper.getMediaVideoList(maxid) }
        val await = mediaImageList.await()
        await.addAll(mediaVideoList.await())
        return await
    }

    suspend fun getMediaImageAndVideoListNoSha1(addVideo: Boolean): ArrayList<Folder> {
        //查看db list
        val mediaInfoListAllNotDeleteFromDB =
            appDataBase.mediaInfoDao?.getMediaInfoListAllNotDelete()
        //获取最新的所有系统media数据
        val mediaInfoListAllFromSystem = mediaInfohelper.getMediaImageVidoeListNoSha1(addVideo)
        //对比系统media 获取需要更新和删除的记录 并插入到db
        var result: ArrayList<MediaInfo>
        if (mediaInfoListAllNotDeleteFromDB != null && mediaInfoListAllNotDeleteFromDB.size > 0) {
            result = ArrayList()
            //db 中已经存在，找出差异
            val same: HashSet<Long> = HashSet()
            //用来存放DB中的已经插入的id，放到map 中，然后扫描新的数据，扫描出未插入的数据
            for (mediainfo in mediaInfoListAllNotDeleteFromDB) {
                if (mediainfo.id != null) {
                    same.add(mediainfo.id!!)
                }
            }
            for (mediainfo in mediaInfoListAllFromSystem) {
                if (mediainfo.id != null) {
                    if (same.add(mediainfo.id!!)) {
                        //db中没有
                        result.add(mediainfo)
                    } else {
                        //db 已经插入了
                        continue
                    }
                }
            }
            LogUtils.d("差量：" + result.size)
            if (result.size > 0) {
                //将差量插入新的数据到db中
                appDataBase.mediaInfoDao?.insertMediaInfo(result)
                result.addAll(mediaInfoListAllNotDeleteFromDB)
            } else {
                result = mediaInfoListAllNotDeleteFromDB as ArrayList<MediaInfo>
            }
            //todo 检查已经插入的和新数据是否删除 这个检测是耗时操作，需要优化
            val iterator = result.iterator();
            while (iterator.hasNext()) {
                val next = iterator.next()
                if(!next.filePath.isNullOrEmpty()){
                    if (!File(next.filePath).exists()) {
                        iterator.remove()
                    }
                }
            }

        } else {
            //db 没有存在，全量插入
            result = mediaInfoListAllFromSystem
            appDataBase.mediaInfoDao?.insertMediaInfo(result)
        }
        //获取最终的list
        return mediaInfohelper.getFolderByMediaInfoList(result)
    }

    suspend fun getUserDirlist(isUserCache: Boolean): MutableList<UserDirBean>? {
        try {
            val dirlist = apiNetWork.getUserDirlist(-1, pageSize = -1)
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
                    ?: DataRepository(
                        ApiNetWork.newInstance(),
                        AppDataBase.getInstanse(),
                        MediaInfoScanHelper.newInstance()
                    ).also { INSTANCE = it }
            }

    }
}