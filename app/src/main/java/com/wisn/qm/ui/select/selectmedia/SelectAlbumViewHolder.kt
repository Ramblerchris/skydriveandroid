package com.wisn.qm.ui.select.selectmedia

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.qmuiteam.qmui.widget.QMUIRadiusImageView2
import com.wisn.qm.R

/**
 * Created by Wisn on 2020/6/6 下午6:18.
 */
open class SelectAlbumViewHolder(view: View) : BaseViewHolder(view) {
    var image: ImageView? = null
    var iv_select: ImageView? = null
    var video_time: TextView? = null
    var iv_isexist: QMUIRadiusImageView2? = null

    init {
        image = view.findViewById(R.id.image)
        iv_select = view.findViewById(R.id.iv_select)
        video_time = view.findViewById(R.id.video_time)
        iv_isexist = view.findViewById(R.id.iv_isexist)
    }
}