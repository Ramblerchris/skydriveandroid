package com.wisn.qm.ui.album

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.Utils
import com.library.base.base.BaseViewModel
import com.wisn.qm.mode.DataRepository
import com.wisn.qm.mode.UserDirListDataSource
import com.wisn.qm.mode.beans.FileType
import com.wisn.qm.mode.beans.PageKey
import com.wisn.qm.mode.db.AppDataBase
import com.wisn.qm.mode.db.beans.UserDirBean
import com.wisn.qm.mode.db.beans.MediaInfo
import com.wisn.qm.mode.db.beans.UploadBean
import com.wisn.qm.mode.net.ApiNetWork
import com.wisn.qm.task.TaskUitls
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AlbumViewModel : BaseViewModel() {
    val userdir = MutableLiveData<UserDirBean>()
    var selectSha1List = ArrayList<String>()
    var dirlistLD = MutableLiveData<MutableList<UserDirBean>>()
    var dirLevel1listLD = MutableLiveData<MutableList<UserDirBean>>()
    var selectData = MutableLiveData<MutableList<UserDirBean>>()
    var userDirListDataSource: UserDirListDataSource? = null
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
        if (isinit) {
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
            selectSha1List.clear()
        }
        userDirBean?.let {
            if (isAdd) {
                selectOnlineData().value?.add(userDirBean)
                selectSha1List.add(userDirBean.sha1!!)
            } else {
                selectOnlineData().value?.remove(userDirBean)
                selectSha1List.remove(userDirBean.sha1!!)
            }
            selectOnlineData().value = selectOnlineData().value
        }

    }

    /**
     * 获取数据
     * prefetchDistance = 10 预加载10条
     */
    fun getDirOnlineListAll(): ArrayList<UserDirBean> {

        if (userDirListDataSource == null) {
            return ArrayList()
        }
        return userDirListDataSource!!.mutableList!!
    }

    fun getDirOnlineListCount(dirName: String?): String? {
        return if (userDirListDataSource != null && userDirListDataSource!!.count != null) {
            "${dirName}(${userDirListDataSource!!.count})"
        } else {
            dirName
        }
    }

    fun getUserDirListDataSource(pid: Long) = Pager(PagingConfig(pageSize = 1, prefetchDistance = 40), PageKey(pid)) {
        userDirListDataSource = UserDirListDataSource()
        return@Pager userDirListDataSource!!
    }.flow
            .cachedIn(viewModelScope)
            .asLiveData(viewModelScope.coroutineContext)

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

    /**
     * 获取相册的所有图片视频
     */
    fun getUserOnlineDirlist(pid: Long): MutableLiveData<MutableList<UserDirBean>> {
        launchGo({
            val dirlist = ApiNetWork.newInstance().getUserDirlist(pid, pageSize = -1)
            if (dirlist.isSuccess()) {
                dirlistLD.value = dirlist.data.list
                selectOnlineData().value?.clear()
                selectSha1List.clear()
            } else {
                defUi.toastEvent.value = dirlist.msg()
            }
            dirlist
        })
        return dirlistLD
    }

    /**
     * 批量删除
     */
    fun deleteOnlinefiles(pid: Long) {
        launchGo({
            var sb = StringBuilder();
            selectSha1List.forEachIndexed { index, s ->
                if (index == (selectSha1List.size - 1)) {
                    sb.append(s)
                } else {
                    sb.append(s + ";")
                }
            }
            val dirlist = ApiNetWork.newInstance().deleteUserfilesByPidAndSha1s(pid, sb.toString())
            if (dirlist.isSuccess()) {
                getUserOnlineDirlist(pid)
                defUi.refresh.call()
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