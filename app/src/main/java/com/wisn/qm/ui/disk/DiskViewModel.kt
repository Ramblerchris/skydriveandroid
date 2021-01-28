package com.wisn.qm.ui.disk

import androidx.lifecycle.MutableLiveData
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.Utils
import com.library.base.base.BaseViewModel
import com.wisn.qm.mode.beans.FileBean
import com.wisn.qm.mode.beans.FileType
import com.wisn.qm.mode.db.AppDataBase
import com.wisn.qm.mode.db.beans.DiskUploadBean
import com.wisn.qm.mode.db.beans.UserDirBean
import com.wisn.qm.mode.net.ApiNetWork
import com.wisn.qm.task.UploadTaskUitls
import com.wisn.qm.ui.view.ViewPosition
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class DiskViewModel : BaseViewModel() {
    val userdir = MutableLiveData<UserDirBean>()
    var result = ArrayList<String>()
    var dirlistLD = MutableLiveData<MutableList<UserDirBean>>()
    var selectData = MutableLiveData<MutableList<UserDirBean>>()
    var currentpid: Long = -1
    var stack: Stack<Long> = Stack()
    var currentViewPosition = MutableLiveData<ViewPosition>()
    private var positionMap = HashMap<Long, ViewPosition>()

    fun selectData(): MutableLiveData<MutableList<UserDirBean>> {
        if (selectData.value == null) {
            selectData.value = ArrayList()
        }
        return selectData
    }

    fun getDiskDirlist(pid: Long, isBack: Boolean = false): MutableLiveData<MutableList<UserDirBean>> {
        launchGo({
            val dirlist = ApiNetWork.newInstance().getDiskDirlist(pid,pageSize = -1)
            if (dirlist.isSuccess()) {
                dirlistLD.value = dirlist.data.list
                addPid(pid, isBack)
                //更新当前位置
                val get = positionMap.get(currentpid)
                get?.let {
                    currentViewPosition.value=get
                }
            }
            dirlist
        })
        return dirlistLD
    }

    private fun addPid(pid: Long, isBack: Boolean = false) {
        if (!isBack) {
            if (stack.isEmpty()) {
                stack.push(currentpid)
            } else {
                val peek = stack.peek();
                if (peek != currentpid) {
                    stack.push(currentpid)
                }
            }
        }
        currentpid = pid
    }

    fun refresh() {
        getDiskDirlist(currentpid)
    }

    fun backFileList(): Boolean {
        try {
            if (currentpid == -1L) {
                return true
            }
            val pop = stack.pop()
            getDiskDirlist(pop, true)
            return false
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun editUserDirBean(isinit: Boolean, isAdd: Boolean, userDirBean: UserDirBean?) {
        if (isinit) {
            selectData().value?.clear();
            result.clear()
        }
        userDirBean?.let {
            if (isAdd) {
                selectData().value?.add(userDirBean)
                result.add(userDirBean.sha1!!)
            } else {
                selectData().value?.remove(userDirBean)
                result.remove(userDirBean.sha1!!)
            }
            selectData().value = selectData().value
        }

    }


    fun addUserDir(filename: String): MutableLiveData<UserDirBean> {
        launchGo({
            val dirlist = ApiNetWork.newInstance().addDiskDir(currentpid, filename)
            if (dirlist.isSuccess()) {
                userdir.value = dirlist.data
                refresh()
            }
            dirlist
        })
        return userdir
    }


    fun deletefiles(pid: Long) {
        launchGo({
            var sb = StringBuilder()
            result.forEachIndexed { index, s ->
                if (index == (result.size - 1)) {
                    sb.append(s)
                } else {
                    sb.append(s + ";")
                }
            }
            val dirlist = ApiNetWork.newInstance().deletefiles(pid, sb.toString())
            if (dirlist.isSuccess()) {
                getDiskDirlist(pid)
            }
            dirlist
        })
    }

    fun setViewPosition(viewPosition: ViewPosition) {
        positionMap.put(currentpid,viewPosition)
    }

    fun saveFileBeanList(selectData: ArrayList<FileBean>) {
        LogUtils.d("saveMedianInfo", Thread.currentThread().name)
        GlobalScope.launch {

            LogUtils.d("saveMedianInfo", Thread.currentThread().name)
            //子线程
            var uploadlist = ArrayList<DiskUploadBean>()
            for (mediainfo in selectData) {
                var diskUploadBean = DiskUploadBean(mediainfo.fileName, mediainfo.filePath, mediainfo.size, currentpid, FileType.UPloadStatus_Noupload)
                uploadlist.add(diskUploadBean)
            }
            LogUtils.d("uploadlist size", uploadlist.size)
            AppDataBase.getInstanse().diskUploadBeanDao?.insertDiskUploadBeanList(uploadlist)
            UploadTaskUitls.exeRequest(Utils.getApp(), UploadTaskUitls.buildDiskUploadWorkerRequest())
        }
    }

}