package com.wisn.qm.task

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.blankj.utilcode.util.LogUtils
import com.jeremyliao.liveeventbus.LiveEventBus
import com.library.base.net.upload.ProgressRequestBody
import com.library.base.utils.NetCheckUtils
import com.library.base.utils.SHAMD5Utils
import com.library.base.utils.UploadTip
import com.wisn.qm.mode.ConstantKey
import com.wisn.qm.mode.beans.FileType
import com.wisn.qm.mode.db.AppDataBase
import com.wisn.qm.mode.db.beans.UploadBean
import com.wisn.qm.mode.net.ApiNetWork
import okhttp3.MultipartBody
import java.io.File


class UploadWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {
    val TAG: String = "UploadWorker"
    override suspend fun doWork(): Result {
        Log.d(TAG, "0000doWork, + ${Thread.currentThread().name}")
        try {
            var isScanContine = true
            while (isScanContine) {
                try {
                    val Noupload =
                        AppDataBase.getInstanse().uploadBeanDao?.getCountByStatus(FileType.UPloadStatus_Noupload)
                    //数量大于0 的情况下开启
                    if (Noupload != null && Noupload > 0) {
                        val uploadDataList =
                            AppDataBase.getInstanse().uploadBeanDao?.getUploadBeanListPreUpload(
                                FileType.UPloadStatus_Noupload
                            )
                        dealUpload(Noupload, uploadDataList)
                    } else {
                        isScanContine = false
                    }
                    Log.d(TAG, "1111doWork, + ${Thread.currentThread().name}")
                } catch (e: Exception) {
                    isScanContine = false
                }
                //防止无法链接的时候无限循环上传
                val connectCheckInit = NetCheckUtils.isConnectCheckInit()
                if (!connectCheckInit) {
                    isScanContine = false
                }
            }
            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure()
        }

    }

    private suspend fun dealUpload(sum: Int, uploadDataList: List<UploadBean>?) {
        if (uploadDataList != null) {
            var position = 0
            for (uploadbean in uploadDataList) {
                try {
                    //查看当前执行的是否已经在其他队列中执行
                    val uploadBeanById =
                        AppDataBase.getInstanse().uploadBeanDao?.getUploadBeanById(uploadbean.id)
                    if (uploadBeanById?.uploadStatus == FileType.UPloadStatus_uploading) {
                        //正在上传的，直接跳过
                        continue
                    }
                    //修改为正在上传中，避免其他的任务重置执行
                    AppDataBase.getInstanse().uploadBeanDao?.updateUploadBeanStatus(
                        uploadbean.id,
                        FileType.UPloadStatus_uploading,
                        System.currentTimeMillis()
                    )
                    LogUtils.d("0000doWork" + uploadbean.toString())
                    //如果sha1为null 先生成sha1
                    if (uploadbean.sha1.isNullOrEmpty()) {
                        uploadbean.sha1 = uploadbean.filePath?.let {
                            LogUtils.d("开始获取sha1" )
                            SHAMD5Utils.getSHA1(uploadbean.filePath)
                        }
                        uploadbean.sha1?.let {
                            upateSha1ById(uploadbean, it)
                        }
                    }
                    if (uploadbean.sha1.isNullOrEmpty()) {
                        //尝试获取后还是null
                        // 删除文件
                        upateStatus(uploadbean, FileType.UPloadStatus_uploadDelete,  FileType.MediainfoStatus_Deleted)
                        continue
                    }

                    //先尝试秒传
                    val uploadFileHitpass = uploadbean.sha1?.let {
                        ApiNetWork.newInstance()
                            .uploadFileHitpass(uploadbean.pid, uploadbean.sha1!!)
                    }
                    var isHitPass = false
                    if (uploadFileHitpass != null && uploadFileHitpass.isUploadSuccess()) {
                        //修改上传成功状态
                        upateStatus(uploadbean, FileType.UPloadStatus_uploadSuccess,  FileType.MediainfoStatus_uploadSuccess,true)
                        isHitPass = true
                    } else {
                        //秒传失败，要重新上传文件
                        uploadFile(uploadbean)
                    }
                    uploadbean.isHitPass = isHitPass
                    position++
                    UploadTip.tipVibrate()
                } catch (e: Exception) {
                    e.printStackTrace()
                    upateStatus(uploadbean, FileType.MediainfoStatus_Noupload, -1)
                }
                var uploadCountProgress: UploadCountProgress =
                    UploadCountProgress(UploadCountProgress.UploadCountProgress_Album, sum)
                val uploadingCount =
                    AppDataBase.getInstanse().uploadBeanDao?.getCountByStatus(FileType.UPloadStatus_uploading)
                val noUploadCount =
                    AppDataBase.getInstanse().uploadBeanDao?.getCountByStatus(FileType.UPloadStatus_Noupload)
                if (noUploadCount != null) {
                    uploadCountProgress.leftsize = noUploadCount
                }
                if (uploadingCount != null) {
                    uploadCountProgress.uploadcount = uploadingCount
                }
                uploadCountProgress.currentUploadBean = uploadbean
                LiveEventBus
                    .get(ConstantKey.uploadingInfo)
//                        .post("上传中${uploadingCount}\n剩余${noUploadCount}")
                    .post(uploadCountProgress)
            }
            val Noupload =
                AppDataBase.getInstanse().uploadBeanDao?.getCountByStatus(FileType.UPloadStatus_Noupload)
            if (Noupload == 0) {
                LiveEventBus
                    .get(ConstantKey.uploadingInfo)
                    .post(UploadCountProgress(UploadCountProgress.UploadCountProgress_Album, true))
            }
            if (position > 0) {
                UploadTip.tipRing()
                LiveEventBus
                    .get(ConstantKey.finishUpdatePhotoList)
                    .postDelay(1, 1000)
                LiveEventBus
                    .get(ConstantKey.updateAlbum)
                    .post(1)
            }
        }
    }

    private suspend fun uploadFile(uploadbean: UploadBean): Boolean {
        val file = File(uploadbean.filePath!!);
        if (file.exists()) {
            try {
                var progressRequestBody = ProgressRequestBody(
                    "${uploadbean.mediainfoid}",
                    UploadFileProgressManager.getInstance(),
                    File(uploadbean.filePath!!),
                    uploadbean.mimeType
                )
                val body =
                    MultipartBody.Part.createFormData(
                        "file",
                        uploadbean.fileName,
                        progressRequestBody
                    )
                val uploadFile = ApiNetWork.newInstance().uploadFile(
                    uploadbean.sha1!!,
                    uploadbean.pid,
                    uploadbean.isVideo!!,
                    uploadbean.mimeType!!,
                    uploadbean.duration!!,
                    body
                )
                if (uploadFile.isUploadSuccess()) {
                    //上传成功
                    upateStatus(uploadbean, FileType.UPloadStatus_uploadSuccess,  FileType.MediainfoStatus_uploadSuccess)
                    LogUtils.d("0000doWork   " + uploadFile.data())
                    return true;
                }
            } catch (e: Exception) {
                //上传异常，重复上传
                upateStatus(uploadbean, FileType.UPloadStatus_Noupload, -1)
            }
        } else {
            //文件不存在，已经删除
            upateStatus(uploadbean, FileType.UPloadStatus_uploadDelete,  FileType.MediainfoStatus_Deleted)
        }
        return false
    }


    suspend fun upateStatus(uploadbean: UploadBean, status: Int ,isUpdateMediaInfostatus: Int,isHitPass:Boolean=false) {
        //文件不存在，已经删除
        AppDataBase.getInstanse().uploadBeanDao?.updateUploadBeanStatus(
            uploadbean.id,
            status,
            System.currentTimeMillis(),
            isHitPass
        )
        uploadbean.uploadStatus = status
        if (isUpdateMediaInfostatus>0) {
            AppDataBase.getInstanse().mediaInfoDao?.updateMediaInfoStatusById(
                uploadbean.mediainfoid!!,
                isUpdateMediaInfostatus
            )
        }
    }

    suspend fun upateSha1ById(uploadbean: UploadBean,sha1:String) {
        //文件不存在，已经删除
        AppDataBase.getInstanse().uploadBeanDao?.updateUploadBeanSha1ById(
            uploadbean.id,
            sha1
        )
        AppDataBase.getInstanse().mediaInfoDao?.updateMediaInfoSha1ById(
            uploadbean.mediainfoid!!, sha1
        )
    }
}