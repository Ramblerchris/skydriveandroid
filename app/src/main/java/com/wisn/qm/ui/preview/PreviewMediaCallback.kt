package com.wisn.qm.ui.preview

import android.view.View
import com.wisn.qm.mode.beans.PreviewImage
import com.wisn.qm.ui.preview.listener.LoadOriginCallBack
import com.wisn.qm.ui.preview.viewholder.PreviewVideoViewHolder

interface PreviewMediaCallback {
    fun callBackLocal(view: View)

    fun callBackOnLine(
        position: Int,
        mediainfo: PreviewImage,
        isShowLoadOrigin: Boolean,
        loadOriginCallBack: LoadOriginCallBack?
    )

    fun playViewPosition(previewVideoViewHolder: PreviewVideoViewHolder, position: Int)
}