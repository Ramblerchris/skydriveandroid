package com.wisn.qm.ui.upload

import android.content.Context
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.library.base.glide.progress.OnProgressListener
import com.library.base.utils.GlideUtils
import com.qmuiteam.qmui.widget.QMUIProgressBar
import com.qmuiteam.qmui.widget.QMUIRadiusImageView2
import com.wisn.qm.R
import com.wisn.qm.mode.beans.FileType
import com.wisn.qm.mode.db.beans.UploadBean
import com.wisn.qm.task.UploadFileProgressManager

/**
 * Created by Wisn on 2020/6/6 下午6:18.
 */
open class UploadViewHolder(view: View) : BaseViewHolder(view), OnProgressListener {
    private var context: Context? = null
    var iv_header: QMUIRadiusImageView2? = null
    var des: TextView? = null
    var name: TextView? = null
    var size: TextView? = null
    var ishitPass: TextView? = null
    var uploaddir: TextView? = null
    var rectProgressBar: QMUIProgressBar? = null
    var ll_rectProgressBar: LinearLayout? = null
    var tv_progress: TextView? = null
    var uploadBean: UploadBean? = null

    init {
        iv_header = view.findViewById(R.id.iv_header)
        des = view.findViewById(R.id.des)
        name = view.findViewById(R.id.name)
        size = view.findViewById(R.id.size)
        ishitPass = view.findViewById(R.id.ishitPass)
        uploaddir = view.findViewById(R.id.uploaddir)
        rectProgressBar = view.findViewById(R.id.rectProgressBar)
        ll_rectProgressBar = view.findViewById(R.id.ll_rectProgressBar)
        tv_progress = view.findViewById(R.id.tv_progress)
    }

    fun loadInfo(context: Context, uploadBean: UploadBean) {
        this.context = context;
        this.uploadBean = uploadBean
        name?.text = uploadBean.fileName
        size?.text = uploadBean.filesizeStr
        if (TextUtils.isEmpty(uploadBean.upDirName)) {
            uploaddir?.visibility = View.GONE
        } else {
            uploaddir?.visibility = View.VISIBLE
            uploaddir?.text = "上传至" + uploadBean.upDirName
        }
        updateStatusStr(uploadBean)
        if (uploadBean.isHitPass == true) {
            ishitPass?.visibility = View.VISIBLE
        } else {
            ishitPass?.visibility = View.GONE
        }
        GlideUtils.loadFile(uploadBean.filePath!!, iv_header!!)
        ll_rectProgressBar?.visibility = View.GONE
        if (uploadBean.uploadStatus == FileType.UPloadStatus_uploading || uploadBean.uploadStatus == FileType.UPloadStatus_Noupload) {
            if (uploadBean.isVideo == true && uploadBean.fileSize!! > 20 * 1024 * 1024) {
                UploadFileProgressManager.getInstance()
                    .addListener("${uploadBean.mediainfoid}", this)
                ll_rectProgressBar?.visibility = View.VISIBLE
                rectProgressBar?.setProgress(0, false)
//                tv_progress?.setText("0%")
            }
        }
    }

    private fun updateStatusStr(
        uploadBean: UploadBean
    ) {
        des?.let {
            it.text = uploadBean.getStatusStr()
            var colorid: Int = if (uploadBean.uploadStatus == FileType.UPloadStatus_uploadSuccess) {
                R.color.green
            } else {
                R.color.red
            }
            val color = it.context.resources?.getColor(colorid);
            if (color != null) {
                it.setTextColor(color)
            }
        }
    }

    override fun onProgress(
        tag: String?,
        isComplete: Boolean,
        percentage: Int,
        bytesWriting: Long,
        totalBytes: Long
    ) {
        uploadBean?.let {
            Log.d(
                "UploadFile",
                "$tag $bytesWriting $totalBytes percentage:$percentage  isComplete:$isComplete"
            )

            if (tag.equals(it.mediainfoid.toString())) {
                if (isComplete) {
                    it.uploadSuccessTime = System.currentTimeMillis();
                    //更新上传
                    ll_rectProgressBar?.visibility = View.GONE
                    updateStatusStr(it)
                } else {
                    ll_rectProgressBar?.visibility = View.VISIBLE
                    rectProgressBar?.setProgress(percentage, true)
//                    tv_progress?.setText("${percentage}%")
                    des?.text="上传中(${percentage}%)"
                }
            }
        }
    }

}