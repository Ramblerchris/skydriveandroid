package com.wisn.qm.ui.upload

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.library.base.utils.GlideUtils
import com.qmuiteam.qmui.widget.QMUIRadiusImageView2
import com.wisn.qm.R
import com.wisn.qm.mode.beans.FileType
import com.wisn.qm.mode.db.beans.UploadBean

/**
 * Created by Wisn on 2020/6/6 下午6:18.
 */
open class UploadViewHolder(view: View) : BaseViewHolder(view) {
    var iv_header: QMUIRadiusImageView2? = null
    var des: TextView? = null
    var name: TextView? = null

    init {
        iv_header = view.findViewById(R.id.iv_header)
        des = view.findViewById(R.id.des)
        name = view.findViewById(R.id.name)
    }
    fun loadInfo(context: Context, uploadBean:UploadBean){
        name?.text=uploadBean.fileName
        des?.text=uploadBean.getStatusStr()
        var colorid:Int=if(uploadBean.uploadStatus==FileType.UPloadStatus_uploadSuccess){
            R.color.green
        }else{
            R.color.red
        }
        des?.setTextColor(context.resources.getColor(colorid))
        GlideUtils.loadFile(uploadBean.filePath!!,iv_header!!)
    }

}