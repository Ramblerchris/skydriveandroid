package com.wisn.qm.ui.select.selectmedia

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.blankj.utilcode.util.LogUtils
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.library.base.utils.GlideUtils
import com.library.base.utils.MToastUtils
import com.qmuiteam.qmui.kotlin.onClick
import com.wisn.qm.R
import com.wisn.qm.mode.beans.FileType
import com.wisn.qm.mode.db.beans.MediaInfo
import com.wisn.qm.ui.select.SelectFileViewModel

/**
 * Created by Wisn on 2020/6/6 下午6:14.
 */

class SelectMediaAdapter(var selectPictureCallBack: SelectFileViewModel, var maxSelect: Int = -1) : BaseMultiItemQuickAdapter<MediaInfo, SelectAlbumViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectAlbumViewHolder {
        val from = LayoutInflater.from(parent.context)
        if (viewType == FileType.ImageViewItem||viewType == FileType.VideoViewItem) {
            return SelectAlbumViewHolder(
                from.inflate(
                    R.layout.rv_item_picture_image,
                    parent,
                    false
                )
            )
        }
        return super.onCreateViewHolder(parent, viewType)
    }



    override fun convert(viewhoder: SelectAlbumViewHolder, item: MediaInfo) {
        val adapterPosition = viewhoder.adapterPosition
        if (item.itemType == FileType.ImageViewItem || item.itemType == FileType.VideoViewItem) {

            LogUtils.d(item.filePath)
            viewhoder.image?.let {
//                Glide.with(context).load(File(item.filePath!!))
//                        .apply(RequestOptions())
//                        .into(it)
                GlideUtils.loadFile(item.filePath!!, it)
                viewhoder.image?.onClick {
                    //如果是要选中
                    if (maxSelect > 0 && !item.isSelect && selectPictureCallBack.selectMediaData().value?.size!! >= maxSelect) {
                        MToastUtils.show("最多选择${maxSelect}张")
                        return@onClick
                    }
                    item.isSelect = !item.isSelect
                    notifyItemChanged(adapterPosition)
                    selectPictureCallBack.changeMediaSelectData(item.isSelect, item)
                }
                viewhoder.iv_select?.visibility = View.VISIBLE
                if (item.isSelect) {
                    viewhoder.iv_select?.setBackgroundResource(R.mipmap.ic_image_selected)
                } else {
                    viewhoder.iv_select?.setBackgroundResource(R.mipmap.ic_image_unselected)
                }

            }
            if (item.uploadStatus == FileType.MediainfoStatus_uploadSuccess) {
                viewhoder.iv_isexist?.visibility = View.VISIBLE
            } else {
                viewhoder.iv_isexist?.visibility = View.GONE
            }
            if (item.isVideo!!) {
                viewhoder.video_time?.visibility = View.VISIBLE
                viewhoder.video_time?.setText(item.timestr)
            } else {
                viewhoder.video_time?.visibility = View.GONE
            }
        }
    }


}

