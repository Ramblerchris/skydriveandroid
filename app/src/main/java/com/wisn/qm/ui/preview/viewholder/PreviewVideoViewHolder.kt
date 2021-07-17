package com.wisn.qm.ui.preview.viewholder

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import com.library.base.utils.GlideUtils
import com.wisn.qm.R
import com.wisn.qm.mode.beans.PreviewImage
import com.wisn.qm.ui.preview.PreviewMediaCallback
import com.wisn.qm.ui.preview.view.PreviewLocalControlView

class PreviewVideoViewHolder(var context: Context, var view: View, var previewCallback: PreviewMediaCallback) : BasePreviewHolder(view) {
    val TAG: String = "PreviewVideoViewHolder"

    var content: FrameLayout = view.findViewById(R.id.content)
    var preview: PreviewLocalControlView = view.findViewById(R.id.preview)
    var pos: Int = -1

    override fun loadVideo(position: Int, mediainfo: PreviewImage) {
        preview.let {
            GlideUtils.loadUrlNoOP(mediainfo.resourcePath!!, preview.thumb!!)
        }
        this.pos = position
        view.tag = this

//        content.onClick {
//            previewCallback.onContentClick(it)
//        }
    }

    override fun releaseVideo(position: Int) {

    }


}