package com.wisn.qm.ui.preview

import android.view.View
import com.wisn.qm.mode.beans.PreviewImage
import com.wisn.qm.ui.preview.viewholder.PreviewVideoViewHolder

interface PreviewMediaCallback {
    fun callBackLocal(view: View)

    fun callBackOnLine(position: Int, mediainfo: PreviewImage, isShowLoadOrigin: Boolean)

    fun playViewPosition(previewVideoViewHolder: PreviewVideoViewHolder, position: Int)
}