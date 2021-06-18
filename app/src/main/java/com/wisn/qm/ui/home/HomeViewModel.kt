package com.wisn.qm.ui.home

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import com.blankj.utilcode.util.ImageUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.PathUtils
import com.blankj.utilcode.util.Utils
import com.library.base.base.BaseViewModel
import com.library.base.config.GlobalUser
import com.library.base.config.UserBean
import com.library.base.event.Message
import com.wisn.qm.BuildConfig
import com.wisn.qm.mode.DataRepository
import com.wisn.qm.mode.beans.FileType
import com.wisn.qm.mode.beans.Update
import com.wisn.qm.mode.db.AppDataBase
import com.wisn.qm.mode.db.beans.Folder
import com.wisn.qm.mode.db.beans.UserDirBean
import com.wisn.qm.mode.db.beans.MediaInfo
import com.wisn.qm.mode.db.beans.UploadBean
import com.wisn.qm.mode.net.ApiNetWork
import com.wisn.qm.task.TaskUitls
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class HomeViewModel : BaseViewModel() {
    private val titleStr = "相册"
    var count: Int = 0
        set(value){
            field=value
            updateHomeTitle()
        }
    var selectData = MutableLiveData<MutableList<MediaInfo>>()
    var folderData = MutableLiveData<ArrayList<Folder>>()
    var dirlistLD = MutableLiveData<MutableList<UserDirBean>>()
    var userdirBean = MutableLiveData<UserDirBean>()
    var usernameData = MutableLiveData<String>()
    var userBean = MutableLiveData<UserBean>()
    var deleteDirs = MutableLiveData<Boolean>()
    var UpdateData = MutableLiveData<Update>()
    var titleShow = MutableLiveData<String>()


    fun changeSelectData(isinit: Boolean, isSelectModel: Boolean, isAdd: Boolean, item: MediaInfo?) {
        if (selectData.value == null) {
            selectData.value = ArrayList<MediaInfo>()
        }
        if (isinit) {
            selectData.value?.clear()
        }
        if (isSelectModel) {
            if (item != null) {
                if (isAdd) {
                    selectData.value?.add(item)
                } else {
                    selectData.value?.remove(item)
                }
                selectData.value = selectData.value
            }
            titleShow.value = "已选中${selectData.value?.size ?: 0}项"
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

    fun getUserDirlist(): MutableLiveData<MutableList<UserDirBean>> {
        launchGoLo({
            val result = DataRepository.getInstance().getUserDirlist(true)
            result?.let {
                dirlistLD.value = result
            }
        })
        return dirlistLD
    }

    /**
     * 蒲公英更新
     */
    fun checkUpdate(): MutableLiveData<Update> {
        launchGoLo({
            val result = DataRepository.getInstance().apiNetWork.checkUpdate(BuildConfig.VERSION_NAME, "")
            if (result != null && result.data != null) {
                if (result.data.buildHaveNewVersion && !result.data.downloadURL.isNullOrEmpty() && !result.data.buildBuildVersion.isNullOrEmpty()) {
                    val toInt = result.data.buildVersionNo?.toInt()
                    toInt?.let {
                        if (toInt > BuildConfig.VERSION_CODE) {
                            UpdateData.value = result.data
                        }
                    }
                }
            }
        })
        return UpdateData
    }

    fun getUserInfo(): MutableLiveData<UserBean> {
        userBean.value = GlobalUser.userinfo
        launchGo({
            val userInfo = ApiNetWork.newInstance().getUserInfo()
            if (userInfo.isSuccess()) {
                userBean.value = userInfo.data
                GlobalUser.saveUserInfo(userInfo.data)
            }
            userInfo
        })
        return userBean
    }

    fun updateDirStatus(ids: String,status :Int): MutableLiveData<Boolean> {
        launchGo({
            val deleteDirss = ApiNetWork.newInstance().updateDirStatus(ids,status)
            if (deleteDirss.isSuccess()) {
                deleteDirs.value = true
            }
            deleteDirss
        }, {

        })
        return deleteDirs
    }

    fun updateUserPhoto(it: Bitmap): MutableLiveData<UserDirBean> {
        var file = File(PathUtils.getInternalAppCachePath() + File.separator + System.currentTimeMillis() + "jpeg");
        ImageUtils.save(it, file, Bitmap.CompressFormat.JPEG)
        return updateUserPhoto(file.absolutePath)
    }

    fun updateUserPhoto(it: String): MutableLiveData<UserDirBean> {
        var file = File(it)
        if (file.exists()) {
            launchGo({
//            suspend fun updateUserPhoto(file: MultipartBody.Part): BaseResult<UserDirBean> {
                var requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                val dirlist = ApiNetWork.newInstance().updateUserPhoto(body)
                if (dirlist.isSuccess()) {
                    userdirBean.value = dirlist.data
                    GlobalUser.userinfo?.let {
                        it.photo_file_sha1 = dirlist.data.sha1!!
                        GlobalUser.saveUserInfo(it)
                    }
                }
                dirlist
            })
        }
        return userdirBean
    }


    fun updateUserName(username: String?): MutableLiveData<String> {
        if (username.isNullOrEmpty()) {
            usernameData.value = GlobalUser.userinfo?.user_name
            return usernameData;
        }
        launchGo({
            val dirlist = ApiNetWork.newInstance().updateUserName(username)
            if (dirlist.isSuccess()) {
                usernameData.value = username
                GlobalUser.updateUserName(username)
            }
            dirlist
        })
        return usernameData
    }

    fun updateUserDirName(id: Long, username: String) {
        launchGo({
            val updateDirName = ApiNetWork.newInstance().updateUserDirName(id, username)
            if (updateDirName.isSuccess()) {
                getUserDirlist()
            }
            updateDirName
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
                    for (mediainfo in selectData.value!!) {
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


    fun saveMedianInfo(position: Int, mediainfo: MediaInfo, isauto: Boolean) {
        LogUtils.d("saveMedianInfo", Thread.currentThread().name)
        GlobalScope.launch {
            var dirinfo = getDirInfo(position)
            dirinfo?.let {
                LogUtils.d("saveMedianInfo", Thread.currentThread().name)
                //子线程
                mediainfo.pid = dirinfo.id
                mediainfo.uploadStatus = FileType.UPloadStatus_Noupload
                val buidUploadBean = TaskUitls.buidUploadBean(mediainfo)
                LogUtils.d("upload mediainfo", mediainfo)
                AppDataBase.getInstanse().uploadBeanDao?.insertUploadBean(buidUploadBean)
                if (isauto) {
                    TaskUitls.exeRequest(Utils.getApp(), TaskUitls.buildUploadRequest())
                }
            }
        }
    }
    fun deleteSelect() {
       defUi.msgEvent.value = Message(100,"正在删除")
        launchUI {
            LogUtils.d("deleteSelect", Thread.currentThread().name)
            launchFlow {
                    LogUtils.d("deleteSelect", Thread.currentThread().name)
                    for (mediainfo in selectData.value!!) {
                        //todo 删除
                        try {
                            File(mediainfo.filePath).delete()
                            AppDataBase.getInstanse().mediaInfoDao?.updateMediaInfoStatusById(mediainfo.id!!, FileType.MediainfoStatus_Deleted)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    selectData.value!!.clear()
                    //更新相册
                    TaskUitls.exeRequest(Utils.getApp(), TaskUitls.buildMediaScanWorkerRequest())

            }.flowOn(Dispatchers.IO).collect {

            }
        }
    }

    fun folderData() {
       defUi.msgEvent.value = Message(100,"刷新了")
        launchUI {
            LogUtils.d("folderData launchUI", Thread.currentThread().name)
            launchFlow {
                LogUtils.d("folderData launchFlow", Thread.currentThread().name)
                val mediaImageVidoeListNoSha1 =
                    DataRepository.getInstance().mediaInfohelper.getMediaImageVidoeListNoSha1(true)
                folderData.postValue(mediaImageVidoeListNoSha1)
            }.flowOn(Dispatchers.IO).collect {
                LogUtils.d("folderData collect", Thread.currentThread().name)
            }
        }
    }

    private suspend fun getDirInfo(position: Int): UserDirBean? {
        var dirinfo1: UserDirBean? = null
        if (dirlistLD.value == null || dirlistLD.value?.size == 0) {
            val dirlist = ApiNetWork.newInstance().addUserDir(-1, "云相册")
            if (dirlist.isSuccess()) {
                dirinfo1 = dirlist.data;
            }
        } else {
            dirinfo1 = dirlistLD.value?.get(position)
        }
        return dirinfo1
    }

}