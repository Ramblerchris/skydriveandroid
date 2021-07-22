package com.wisn.qm.ui.preview.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.wisn.qm.mode.beans.PreviewImage

open class BasePreviewHolder(view: View) : RecyclerView.ViewHolder(view) {

    open fun loadImage(position:Int,mediainfo: PreviewImage) {

    }

    open fun loadVideo(position:Int,mediainfo: PreviewImage) {

    }

    open fun releaseVideo(position:Int ,mediainfo: PreviewImage) {

    }


}