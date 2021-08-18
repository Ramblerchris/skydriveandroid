package com.wisn.qm.task

import android.content.Context
import android.text.TextUtils
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


class DiskUploadWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {
    val TAG: String = "DiskUploadWorker"
    override suspend fun doWork(): Result {
        Log.d(TAG, "0000 DiskUploadWorker, + ${Thread.currentThread().name}")
        try {
            val uploadDataList =
                AppDataBase.getInstanse().diskUploadBeanDao?.getDiskUploadBeanListPreUpload(FileType.UPloadStatus_Noupload)
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
                            AppDataBase.getInstanse().diskUploadBeanDao?.updateDiskUploadBeanSha1(
                                diskUploadbean.id,
                                it
                            )
                        }
                    }
                    position++
                    if (!TextUtils.isEmpty(diskUploadbean.sha1)) {
                        //先尝试秒传
                        val uploadFileHitpass = try {
                            ApiNetWork.newInstance()
                                .uploadDiskFileHitpass(diskUploadbean.pid, diskUploadbean.sha1!!)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            null;
                        }
                        if (uploadFileHitpass != null && uploadFileHitpass.isUploadSuccess()) {
                            //修改上传成功状态
                            AppDataBase.getInstanse().diskUploadBeanDao?.updateDiskUploadBeanStatus(
                                diskUploadbean.id,
                                FileType.UPloadStatus_uploadSuccess,
                                System.currentTimeMillis()
                            )
                        } else {
                            uploadFile(diskUploadbean)
                        }
                    } else {
                        AppDataBase.getInstanse().diskUploadBeanDao?.updateDiskUploadBeanStatus(
                            diskUploadbean.id,
                            FileType.UPloadStatus_uploadDelete,
                            System.currentTimeMillis()
                        )
                    }

                    UploadTip.tipVibrate()
                    if (position == size) {
                        LiveEventBus
                            .get(ConstantKey.uploadingInfo)
                            .post(
                                UploadCountProgress(
                                    UploadCountProgress.UploadCountProgress_Disk,
                                    true
                                )
                            )
                    } else {
                        var uploadCountProgress: UploadCountProgress =
                            UploadCountProgress(UploadCountProgress.UploadCountProgress_Disk, size)
                        uploadCountProgress.leftsize = size - position
                        uploadCountProgress.uploadcount = 1
                        LiveEventBus
                            .get(ConstantKey.uploadingInfo)
//                        .post("上传中${uploadingCount}\n剩余${noUploadCount}")
                            .post(uploadCountProgress)
                    }
                }
                if (position > 0) {
                    UploadTip.tipRing()
                    LiveEventBus
                        .get(ConstantKey.finishUpdateDiskList)
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
        val file = File(uploadbean.filePath!!)
        if (file.exists()) {
            try {
                var mimetype = uploadbean.mimeType ?: "multipart/form-data"
                var requestFile =
                    RequestBody.create(MediaType.parse(mimetype), File(uploadbean.filePath!!))
                val body =
                    MultipartBody.Part.createFormData("file", uploadbean.fileName, requestFile)
                val uploadFile = ApiNetWork.newInstance()
                    .uploadDiskFile(uploadbean.sha1!!, uploadbean.pid, mimetype, body)
                if (uploadFile.isUploadSuccess()) {
                    AppDataBase.getInstanse().diskUploadBeanDao?.updateDiskUploadBeanStatus(
                        uploadbean.id,
                        FileType.UPloadStatus_uploadSuccess,
                        System.currentTimeMillis()
                    )
                    LogUtils.d("0000doWork   " + uploadFile.data())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            AppDataBase.getInstanse().diskUploadBeanDao?.updateDiskUploadBeanStatus(
                uploadbean.id,
                FileType.UPloadStatus_uploadDelete,
                System.currentTimeMillis()
            )
        }
    }
}