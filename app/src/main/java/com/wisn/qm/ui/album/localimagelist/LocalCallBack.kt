package com.wisn.qm.ui.album.localimagelist

import com.qmuiteam.qmui.arch.QMUIFragment
import com.wisn.qm.mode.db.beans.MediaInfo

interface LocalCallBack {
    fun showPictureControl(isShow: Boolean?)
    fun changeSelectData(
        isinit: Boolean,
        isSelectModel: Boolean,
        isSelectAll: Boolean,
        isAdd: Boolean,
        selectList: MutableList<MediaInfo>?
    )

    fun getQMUIFragment(): QMUIFragment
}