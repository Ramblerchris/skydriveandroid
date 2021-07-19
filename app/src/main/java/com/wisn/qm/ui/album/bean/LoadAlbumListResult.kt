package com.wisn.qm.ui.album.bean

import com.wisn.qm.mode.db.beans.UserDirBean

class LoadAlbumListResult {

    var selectSha1List: MutableList<UserDirBean>?=null;
    //删除更新
    var isDeleteUpdate:Boolean=false
    //是否是第一页
    var isFirstPage:Boolean=true
    //0 加载成功，1 加载完成，没有更多了，2加载失败
    var loadMoreStatus:Int=1
    var total:Int=1

    companion object {
        const val LoadMore_complete = 0
        const val LoadMore_end = 1
        const val LoadMore_error = 2
    }

}