package com.wisn.qm.task

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.blankj.utilcode.util.LogUtils
import com.jeremyliao.liveeventbus.LiveEventBus
import com.library.base.utils.SHAMD5Utils
import com.library.base.utils.UploadTip
import com.wisn.qm.mode.ConstantKey
import com.wisn.qm.mode.beans.FileType
import com.wisn.qm.mode.db.AppDataBase
import com.wisn.qm.mode.db.beans.UploadBean
import com.wisn.qm.mode.net.ApiNetWork
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File


class UploadWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        println("0000doWork, + ${Thread.currentThread().name}")

        val job = GlobalScope.launch {
            try {// launch a new coroutine and keep a reference to its Job
//                val newInstance = AppDataBaseHelper.newInstance(Utils.getApp())
                val Noupload = AppDataBase.getInstanse().uploadBeanDao?.getCountByStatus(FileType.UPloadStatus_Noupload)
                Noupload?.let {
                    //数量大于0 的情况下开启
                    if (it > 0) {
                        val uploadDataList = AppDataBase.getInstanse().uploadBeanDao?.getUploadBeanListPreUpload(FileType.UPloadStatus_Noupload)
                        dealUpload(uploadDataList)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        return Result.success()
    }

    private suspend fun dealUpload(uploadDataList: List<UploadBean>?) {
        if (uploadDataList != null) {
            var position = 0
            for (uploadbean in uploadDataList) {
                try {
                    //查看当前执行的是否已经在其他队列中执行
                    val uploadBeanById = AppDataBase.getInstanse().uploadBeanDao?.getUploadBeanById(uploadbean.id)
                    if (uploadBeanById?.uploadStatus == FileType.UPloadStatus_uploading) {
                        //正在上传的，直接跳过
                        continue
                    }
                    //修改为正在上传中，避免其他的任务重置执行
                    AppDataBase.getInstanse().uploadBeanDao?.updateUploadBeanStatus(uploadbean.id, FileType.UPloadStatus_uploading, System.currentTimeMillis())
                    LogUtils.d("0000doWork" + uploadbean.toString())
                    //如果sha1为null 先生成sha1
                    if (uploadbean.sha1.isNullOrEmpty()) {
                        uploadbean.sha1 = uploadbean.filePath?.let {
                            SHAMD5Utils.getSHA1(uploadbean.filePath)
                        }
                    }
                    val uploadingCount = AppDataBase.getInstanse().uploadBeanDao?.getCountByStatus(FileType.UPloadStatus_uploading)
                    val noUploadCount = AppDataBase.getInstanse().uploadBeanDao?.getCountByStatus(FileType.UPloadStatus_Noupload)
                    LiveEventBus
                            .get(ConstantKey.uploadingInfo)
                            .post("上传中${uploadingCount}\n剩余${noUploadCount}")
                    //先尝试秒传
                    val uploadFileHitpass = uploadbean.sha1?.let {
                        ApiNetWork.newInstance().uploadFileHitpass(uploadbean.pid, uploadbean.sha1!!)
                    }
                    if (uploadFileHitpass != null && uploadFileHitpass.isUploadSuccess()) {
                        //修改上传成功状态
                        AppDataBase.getInstanse().uploadBeanDao?.updateUploadBeanStatus(uploadbean.id, FileType.UPloadStatus_uploadSuccess, System.currentTimeMillis())
                        AppDataBase.getInstanse().mediaInfoDao?.updateMediaInfoStatusById(uploadbean.mediainfoid!!, FileType.MediainfoStatus_uploadSuccess)
                    } else {
                        //秒传失败，要重新上传文件
                        uploadFile(uploadbean)
                    }
                    position++
                    UploadTip.tipVibrate()
                } catch (e: Exception) {
                    e.printStackTrace()
                    AppDataBase.getInstanse().uploadBeanDao?.updateUploadBeanStatus(uploadbean.id, FileType.UPloadStatus_Noupload, System.currentTimeMillis())
                }
            }
            val Noupload = AppDataBase.getInstanse().uploadBeanDao?.getCountByStatus(FileType.UPloadStatus_Noupload)
            if (Noupload == 0) {
                LiveEventBus
                        .get(ConstantKey.uploadingInfo)
                        .post("上传完成")
            }
    //                    LogUtils.d("0000doWork  LiveEventBus")
            if (position > 0) {
                UploadTip.tipRing()

                LiveEventBus
                        .get(ConstantKey.updatePhotoList)
                        .postDelay(1, 1000)
                LiveEventBus
                        .get(ConstantKey.updateAlbum)
                        .post(1)
            }
        }
    }

    private suspend fun uploadFile(uploadbean: UploadBean) {
        val file = File(uploadbean.filePath!!);
        if (file.exists()) {
            var mimetype = uploadbean.mimeType ?: "multipart/form-data"
            var requestFile = RequestBody.create(MediaType.parse(mimetype), File(uploadbean.filePath!!))
            val body = MultipartBody.Part.createFormData("file", uploadbean.fileName, requestFile)
            val uploadFile = ApiNetWork.newInstance().uploadFile(uploadbean.sha1!!, uploadbean.pid, uploadbean.isVideo!!, uploadbean.mimeType!!, uploadbean.duration!!, body)
            if (uploadFile.isUploadSuccess()) {
                AppDataBase.getInstanse().uploadBeanDao?.updateUploadBeanStatus(uploadbean.id, FileType.UPloadStatus_uploadSuccess, System.currentTimeMillis())
                AppDataBase.getInstanse().mediaInfoDao?.updateMediaInfoStatusById(uploadbean.mediainfoid!!, FileType.MediainfoStatus_uploadSuccess)
                LogUtils.d("0000doWork   " + uploadFile.data())
            }
        } else {
            AppDataBase.getInstanse().uploadBeanDao?.updateUploadBeanStatus(uploadbean.id, FileType.UPloadStatus_uploadDelete, System.currentTimeMillis())
            AppDataBase.getInstanse().mediaInfoDao?.updateMediaInfoStatusBySha1(uploadbean.sha1!!, FileType.MediainfoStatus_Deleted)
        }
    }
}