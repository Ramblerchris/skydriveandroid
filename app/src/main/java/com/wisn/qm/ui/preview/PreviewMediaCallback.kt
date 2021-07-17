package com.wisn.qm.ui.preview

import android.view.View
import com.wisn.qm.ui.preview.viewholder.PreviewVideoViewHolder

interface PreviewMediaCallback {
    fun onContentClick(view : View)

    fun playViewPosition(previewVideoViewHolder : PreviewVideoViewHolder, position:Int)
}