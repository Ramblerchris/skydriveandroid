package com.wisn.qm.ui.previewloc

import android.view.View

interface PreviewCallback {
    fun onContentClick(view : View)

    fun playViewPosition(previewVideoViewHolder : PreviewVideoViewHolder,position:Int)
}