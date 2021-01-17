package com.wisn.qm.ui.select.selectmedia

import com.wisn.qm.mode.db.beans.MediaInfo
 interface SelectPictureCallBack {
    fun changeSelectData(isAdd:Boolean,item: MediaInfo?);
}