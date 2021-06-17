package com.wisn.qm.ui.album.local

import com.wisn.qm.mode.db.beans.MediaInfo
import com.wisn.qm.ui.home.HomeFragment
 interface LocalCallBack {
    fun showPictureControl(isShow: Boolean?)
    fun changeSelectData(isinit:Boolean ,isSelectModel:Boolean,isAdd:Boolean,item: MediaInfo?)
    fun getHomeFragment(): HomeFragment
}