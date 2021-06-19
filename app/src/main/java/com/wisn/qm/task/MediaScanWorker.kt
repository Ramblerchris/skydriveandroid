package com.wisn.qm.task

import android.content.Context
import android.text.TextUtils
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.blankj.utilcode.util.LogUtils
import com.jeremyliao.liveeventbus.LiveEventBus
import com.library.base.net.ExceptionHandle
import com.wisn.qm.mode.ConstantKey
import com.wisn.qm.mode.DataRepository
import com.wisn.qm.mode.beans.FileType
import com.wisn.qm.mode.db.AppDataBase
import com.wisn.qm.mode.db.beans.MediaInfo
import com.wisn.qm.mode.net.ApiNetWork
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

/**
 * 通过最大id 的方式更新扫描，这种方式会造成有些数据无法扫描到
 */
@Deprecated("采用新的比较更新方式")
class MediaScanWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    val TAG:String="MediaScanWorker"
    override fun doWork(): Result {
        GlobalScope.launch {
            try {
                var s = System.currentTimeMillis();

                LogUtils.d(TAG," 开始")

                val maxId = AppDataBase.getInstanse().mediaInfoDao?.getMediaInfoMaxId();

                LogUtils.d(TAG,"  查询最大值maxId用时："+(System.currentTimeMillis() - s))

                if (maxId != null && maxId > 0) {

                    LogUtils.d(TAG, "  差量 $maxId")
                    var start = System.currentTimeMillis();
                    // todo 再次扫描更新
                    val mediaImageListNew = DataRepository.getInstance().getMediaImageAndVideoList(maxId.toString())
                    var start11 = System.currentTimeMillis();
                    LogUtils.d(TAG," 查询最大值maxId列表用时： " + (start11 - start))

                    var mediaInfoListAll = AppDataBase.getInstanse().mediaInfoDao?.getMediaInfoListAllNotDelete()
                    var start22 = System.currentTimeMillis();
                    LogUtils.d(TAG," 查询不是未删除列表用时： " + (start22 - start11))

                    mediaImageListNew?.let {
                        if (mediaInfoListAll != null && mediaInfoListAll!!.isNotEmpty()) {
                            mediaInfoListAll!!.addAll(mediaImageListNew)
                        }
                    }
                    var start33 = System.currentTimeMillis();

                    LogUtils.d(TAG," 合并新的和旧的列表用时 " + (start33 - start22))


                    mediaInfoListAll?.sortByDescending {
                        it.createTime
                    }
                    LogUtils.d(TAG," 按照创建时间排序用时 " + ( System.currentTimeMillis() - start33))

                    var start1 = System.currentTimeMillis()
                    if (mediaInfoListAll==null){
                        mediaInfoListAll=ArrayList<MediaInfo>()
                    }
                    // 先通知显示
                    LiveEventBus
                            .get(ConstantKey.updateHomeMedialist)
                            .post(mediaInfoListAll)
                    mediaImageListNew?.let {
                        AppDataBase.getInstanse().mediaInfoDao?.insertMediaInfo(mediaImageListNew)
                    }
                    LogUtils.d(TAG," 插入新增的列表用时" + (System.currentTimeMillis() - start1))
                    LogUtils.d(TAG," 从扫描到插入的所有的耗时" + (System.currentTimeMillis() - s))
                    var end2 = System.currentTimeMillis();
                    dealUploadStatus(mediaInfoListAll)
                    LogUtils.d(TAG," 处理已经已经上传和删除数据耗时" + (System.currentTimeMillis() - end2))


                } else {
                    LogUtils.d(TAG,"  全量扫描开始")
                    //第一次加载
                    var start = System.currentTimeMillis();
                    //首次 先扫描，后通知显示
                    val mediaImageList = DataRepository.getInstance().getMediaImageAndVideoList("-1")
                    LogUtils.d(TAG," 全量处理扫描所有数据耗时" + (System.currentTimeMillis() - start))
                    LiveEventBus
                            .get(ConstantKey.updateHomeMedialist)
                            .post(mediaImageList)
                    var end2 = System.currentTimeMillis();
                    mediaImageList?.let {
                        mediaImageList?.sortByDescending {
                            it.createTime
                        }
                        LogUtils.d(TAG," 全量扫描排序耗时" + (System.currentTimeMillis() - end2))
                        AppDataBase.getInstanse().mediaInfoDao?.insertMediaInfo(mediaImageList)
                        LogUtils.d(TAG," 全量处理插入所有耗时" + (System.currentTimeMillis() - end2))
                    }
                    var end3 = System.currentTimeMillis();
                    dealUploadStatus(mediaImageList)
                    LogUtils.d(TAG," 全量处理已经已经上传和删除数据耗时" + (System.currentTimeMillis() - end3))
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return Result.success()
    }

    private suspend fun dealUploadStatus(mediaImageList: MutableList<MediaInfo>?) {
        try {
            var allSha1sByUser = ApiNetWork.newInstance().getAllSha1sByUser()
            val data = allSha1sByUser.data
            mediaImageList?: return
            var isUpdate = false
            for (mediainfo in mediaImageList){
                if( mediainfo.uploadStatus !=FileType.MediainfoStatus_uploadSuccess){
                    //如果没有上传成功的再次检查
                    if (!TextUtils.isEmpty(mediainfo.sha1)) {
                        val contains = data.contains(mediainfo.sha1)
                        if (contains) {
                            isUpdate = true
                            mediainfo.uploadStatus = FileType.MediainfoStatus_uploadSuccess
                            mediainfo.id?.let {
                                AppDataBase.getInstanse().mediaInfoDao?.updateMediaInfoStatusById(mediainfo.id!!, FileType.MediainfoStatus_uploadSuccess)
                            }
                        }
                    }
                }
                var file = File(mediainfo.filePath)
                //文件不存在或者文件的大小为0 都不要展示
                if (!file.exists() ||file.length() <= 0 ) {
                    isUpdate = true
                    AppDataBase.getInstanse().mediaInfoDao?.updateMediaInfoStatusById(mediainfo.id!!, FileType.MediainfoStatus_Deleted)
                }
            }
            if (isUpdate) {
                var mediaInfoListAll = AppDataBase.getInstanse().mediaInfoDao?.getMediaInfoListAllNotDelete()
                mediaInfoListAll?.sortByDescending {
                    it.createTime
                }
                LiveEventBus
                        .get(ConstantKey.updateHomeMedialist)
                        .post(mediaInfoListAll);
            }
        } catch (e: Throwable) {
            ExceptionHandle.handleException(e)
        }
    }
}