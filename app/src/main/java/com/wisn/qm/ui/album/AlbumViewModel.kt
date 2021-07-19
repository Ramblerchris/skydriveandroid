package com.wisn.qm.ui.album

import androidx.lifecycle.MutableLiveData
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.Utils
import com.library.base.base.BaseViewModel
import com.wisn.qm.mode.DataRepository
import com.wisn.qm.mode.beans.FileType
import com.wisn.qm.mode.db.AppDataBase
import com.wisn.qm.mode.db.beans.UserDirBean
import com.wisn.qm.mode.db.beans.MediaInfo
import com.wisn.qm.mode.db.beans.UploadBean
import com.wisn.qm.mode.net.ApiNetWork
import com.wisn.qm.task.TaskUitls
import com.wisn.qm.ui.album.bean.LoadAlbumListResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

class AlbumViewModel : BaseViewModel() {
    val userdir = MutableLiveData<UserDirBean>()
    var loadAlbumListResult = MutableLiveData<LoadAlbumListResult>()
    var dirLevel1listLD = MutableLiveData<MutableList<UserDirBean>>()
    var selectData = MutableLiveData<MutableList<UserDirBean>>()
    var selectLocalMediainfoListData = MutableLiveData<MutableList<MediaInfo>>()
    var titleShow = MutableLiveData<String>()
    var titleStr:String? =""

    var count: Int = 0
        set(value){
            field=value
            updateHomeTitle()
        }


    fun changeSelectData(isinit: Boolean, isSelectModel: Boolean, isSelectAll: Boolean, isAdd: Boolean, selectList: MutableList<MediaInfo>?) {
        if (selectLocalMediainfoListData.value == null) {
            selectLocalMediainfoListData.value = ArrayList<MediaInfo>()
        }
        if (isinit||isSelectAll) {
            selectLocalMediainfoListData.value?.clear()
        }
        if (isSelectModel) {
            if (selectList != null) {
                if (isAdd) {
                    selectLocalMediainfoListData.value?.addAll(selectList)
                } else {
                    selectLocalMediainfoListData.value?.removeAll(selectList)
                }
                selectLocalMediainfoListData.value = selectLocalMediainfoListData.value
            }
            titleShow.value = "已选中${selectLocalMediainfoListData.value?.size ?: 0}项"
        } else {
            updateHomeTitle()
        }
    }

    fun deleteSelect() {
//        defUi.msgEvent.value = Message(100,"正在删除")
        count -= selectLocalMediainfoListData.value!!.size
        GlobalScope.launch {
            val iterator = selectLocalMediainfoListData.value!!.iterator()
            while (iterator.hasNext()) {
                val mediainfo = iterator.next();
                try {
                    AppDataBase.getInstanse().mediaInfoDao?.updateMediaInfoStatusById(
                        mediainfo.id!!,
                        FileType.MediainfoStatus_Deleted
                    )
                    mediainfo.filePath?.let {
                        File(it).delete()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            selectLocalMediainfoListData.value!!.clear()
        }
    }

    private fun updateHomeTitle() {
        if (count > 0) {
            titleShow.value = "$titleStr($count)"
        } else {
            titleShow.value = "$titleStr"
        }
    }


    fun selectOnlineData(): MutableLiveData<MutableList<UserDirBean>> {
        if (selectData.value == null) {
            selectData.value = ArrayList<UserDirBean>()
        }
        return selectData
    }

    fun editOnlineUserDirBean(isinit: Boolean, isAdd: Boolean, userDirBean: UserDirBean?) {
        if (isinit) {
            selectOnlineData().value?.clear()
        }
        userDirBean?.let {
            if (isAdd) {
                selectOnlineData().value?.add(userDirBean)
            } else {
                selectOnlineData().value?.remove(userDirBean)
            }
            selectOnlineData().value = selectOnlineData().value
        }

    }


    /**
     * 新建文件夹
     */
    fun addUserDir(filename: String): MutableLiveData<UserDirBean> {
        launchGo({
            val dirlist = ApiNetWork.newInstance().addUserDir(-1, filename)
            if (dirlist.isSuccess()) {
                userdir.value = dirlist.data
            }
            dirlist
        })
        return userdir
    }


    var lastid=-1L;
    fun getloadAlbumListResult(pid: Long,isRefresh:Boolean): MutableLiveData<LoadAlbumListResult> {
        launchGo({
            var loadAlbumListResultBean= LoadAlbumListResult()
            if(isRefresh){
                lastid=-1
            }
            loadAlbumListResultBean.isFirstPage=isRefresh
            val dirlist = ApiNetWork.newInstance().getUserFileAlllist(pid, 20,lastid)
            if (dirlist.isSuccess()) {
                loadAlbumListResultBean.selectSha1List = dirlist.data.list
                //判断是否有下一页
                if(lastid!=dirlist.data.nextpageid&&dirlist.data.list.size>0){
                    //有下一页
                    loadAlbumListResultBean.loadMoreStatus= LoadAlbumListResult.LoadMore_complete
                    lastid=dirlist.data.nextpageid!!
                }else{
                    //数据已经加载完成
                    loadAlbumListResultBean.loadMoreStatus= LoadAlbumListResult.LoadMore_end
                }
                selectOnlineData().value?.clear()
                dirlist.data.total?.let {
                    loadAlbumListResultBean.total= it.toInt()
                }
            } else {
                loadAlbumListResultBean.loadMoreStatus= LoadAlbumListResult.LoadMore_error
                defUi.toastEvent.value = dirlist.msg()
            }
            loadAlbumListResult.value=loadAlbumListResultBean
            dirlist
        },error = {
            var loadAlbumListResultBean= LoadAlbumListResult()
            loadAlbumListResultBean.isFirstPage=isRefresh
            loadAlbumListResultBean.loadMoreStatus= LoadAlbumListResult.LoadMore_error
            loadAlbumListResult.value=loadAlbumListResultBean
        })
        return loadAlbumListResult
    }

    /**
     * 批量删除
     */
    fun deleteOnlinefiles(pid: Long) {
        launchGo({
            var sb = StringBuilder()
            selectData.value?.let {
                it.forEachIndexed { index, s ->
                if (index == (it.size - 1)) {
                    sb.append(s.sha1)
                } else {
                    sb.append(s.sha1 + ";")
                }
            }
            }
            val dirlist = ApiNetWork.newInstance().deleteUserfilesByPidAndSha1s(pid, sb.toString())
            if (dirlist.isSuccess()) {
                var loadAlbumListResultBean= LoadAlbumListResult()
                loadAlbumListResultBean.isDeleteUpdate=true
                loadAlbumListResultBean.selectSha1List=selectData.value
                loadAlbumListResult.value=loadAlbumListResultBean
            }
            dirlist
        })
    }

    fun saveMedianInfo(position: Int) {
        saveMedianInfo(position, true)
    }

    fun saveMedianInfo(position: Int, isauto: Boolean) {
        launchUI {
            LogUtils.d("saveMedianInfo", Thread.currentThread().name)
            launchFlow {
                var dirinfo = getDirInfo(position)
                dirinfo?.let {
                    LogUtils.d("saveMedianInfo", Thread.currentThread().name)
                    //子线程
                    var uploadlist = ArrayList<UploadBean>()
                    for (mediainfo in selectLocalMediainfoListData.value!!) {
                        mediainfo.pid = dirinfo.id
                        mediainfo.uploadStatus = FileType.UPloadStatus_Noupload
                        uploadlist.add(TaskUitls.buidUploadBean(mediainfo))
                    }
                    LogUtils.d("uploadlist size", uploadlist.size)
                    AppDataBase.getInstanse().uploadBeanDao?.insertUploadBeanList(uploadlist)
                    if (isauto) {
                        TaskUitls.exeRequest(Utils.getApp(), TaskUitls.buildUploadRequest())
                    }
                }

            }.flowOn(Dispatchers.IO).collect {

            }
        }
    }

    fun getUserDirlist(): MutableLiveData<MutableList<UserDirBean>> {
        launchGoLo({
            val result = DataRepository.getInstance().getUserDirlist(true)
            result?.let {
                dirLevel1listLD.value = result
            }
        })
        return dirLevel1listLD
    }

    private suspend fun getDirInfo(position: Int): UserDirBean? {
        var dirinfo1: UserDirBean? = null
        if (dirLevel1listLD.value == null || dirLevel1listLD.value?.size == 0) {
            val dirlist = ApiNetWork.newInstance().addUserDir(-1, "云相册")
            if (dirlist.isSuccess()) {
                dirinfo1 = dirlist.data;
            }
        } else {
            dirinfo1 = dirLevel1listLD.value?.get(position)
        }
        return dirinfo1
    }


    /**
     * 添加新的照片到相册中
     */
    fun saveMedianInfo(selectData: ArrayList<MediaInfo>, get: UserDirBean) {
        LogUtils.d("saveMedianInfo", Thread.currentThread().name)
        GlobalScope.launch {

            LogUtils.d("saveMedianInfo", Thread.currentThread().name)
            //子线程
            var uploadlist = ArrayList<UploadBean>()
            for (mediainfo in selectData) {
                mediainfo.filePath?.let {
                    mediainfo.pid = get.id
                    mediainfo.uploadStatus = FileType.UPloadStatus_Noupload
                    uploadlist.add(TaskUitls.buidUploadBean(mediainfo))
                }
            }
            LogUtils.d("uploadlist size", uploadlist.size)
            AppDataBase.getInstanse().uploadBeanDao?.insertUploadBeanList(uploadlist)
            TaskUitls.exeRequest(Utils.getApp(), TaskUitls.buildUploadRequest())
        }
    }

}