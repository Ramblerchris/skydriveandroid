package com.wisn.qm.ui.album.local

import com.qmuiteam.qmui.arch.QMUIFragment
import com.wisn.qm.mode.db.beans.MediaInfo
 interface LocalCallBack {
    fun showPictureControl(isShow: Boolean?)
    fun changeSelectData(isinit:Boolean ,isSelectModel:Boolean,isAdd:Boolean,item: MediaInfo?)
    fun getQMUIFragment(): QMUIFragment
}