package com.wisn.qm.task

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.blankj.utilcode.util.LogUtils
import com.jeremyliao.liveeventbus.LiveEventBus
import com.library.base.utils.SHAMD5Utils
import com.library.base.utils.UploadTip
import com.wisn.qm.mode.ConstantKey
import com.wisn.qm.mode.beans.FileType
import com.wisn.qm.mode.db.AppDataBase
import com.wisn.qm.mode.db.beans.DiskUploadBean
import com.wisn.qm.mode.net.ApiNetWork
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File


class DiskUploadWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {
    val TAG: String = "DiskUploadWorker"
    override suspend fun doWork(): Result {
        Log.d(TAG,"0000 DiskUploadWorker, + ${Thread.currentThread().name}")
        try {
            val uploadDataList = AppDataBase.getInstanse().diskUploadBeanDao?.getDiskUploadBeanListPreUpload(FileType.UPloadStatus_Noupload)
            if (uploadDataList != null) {
                val size = uploadDataList.size
                var position = 0
                for (diskUploadbean in uploadDataList) {
                    LogUtils.d("0000doWork" + diskUploadbean.toString())
                    //如果sha1为null 先生成sha1
                    if (diskUploadbean.sha1.isNullOrEmpty()) {
                        diskUploadbean.sha1 = diskUploadbean.filePath?.let {
                            SHAMD5Utils.getSHA1(diskUploadbean.filePath)
                        }
                        diskUploadbean.sha1?.let {
                            AppDataBase.getInstanse().diskUploadBeanDao?.updateDiskUploadBeanSha1(diskUploadbean.id, it)
                        }
                    }
                    position++
                    LiveEventBus
                        .get(ConstantKey.uploadingInfo)
                        .post("上传中(${position}/${size})")
                    //先尝试秒传
                    val uploadFileHitpass = diskUploadbean.sha1?.let {
                        ApiNetWork.newInstance().uploadDiskFileHitpass(diskUploadbean.pid, diskUploadbean.sha1!!)
                    }
                    if (uploadFileHitpass != null && uploadFileHitpass.isUploadSuccess()) {
                        //修改上传成功状态
                        AppDataBase.getInstanse().diskUploadBeanDao?.updateDiskUploadBeanStatus(diskUploadbean.id, FileType.UPloadStatus_uploadSuccess, System.currentTimeMillis())
                    } else {
                        uploadFile(diskUploadbean)
                    }
                    UploadTip.tipVibrate()
                    if (position == size) {
                        LiveEventBus
                            .get(ConstantKey.uploadingInfo)
                            .post("上传完成")
                    }
                }
                if (position > 0) {
                    UploadTip.tipRing()
                    LiveEventBus
                        .get(ConstantKey.updateDiskList)
                        .post(1)
                }
            }
            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure()
        }

    }

    private suspend fun uploadFile(uploadbean: DiskUploadBean) {
        val file = File(uploadbean.filePath!!);
        if (file.exists()) {
            var mimetype = uploadbean.mimeType ?: "multipart/form-data"
            var requestFile = RequestBody.create(MediaType.parse(mimetype), File(uploadbean.filePath!!))
            val body = MultipartBody.Part.createFormData("file", uploadbean.fileName, requestFile)
            val uploadFile = ApiNetWork.newInstance().uploadDiskFile(uploadbean.sha1!!, uploadbean.pid, mimetype, body)
            if (uploadFile.isUploadSuccess()) {
                AppDataBase.getInstanse().diskUploadBeanDao?.updateDiskUploadBeanStatus(uploadbean.id, FileType.UPloadStatus_uploadSuccess, System.currentTimeMillis())
                LogUtils.d("0000doWork   " + uploadFile.data())

            }
        } else {
            AppDataBase.getInstanse().diskUploadBeanDao?.updateDiskUploadBeanStatus(uploadbean.id, FileType.UPloadStatus_uploadDelete, System.currentTimeMillis())
        }
    }
}