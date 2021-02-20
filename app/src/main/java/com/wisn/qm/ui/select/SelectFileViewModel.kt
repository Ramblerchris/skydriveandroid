package com.wisn.qm.ui.select

import android.os.Environment
import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import com.blankj.utilcode.util.LogUtils
import com.library.base.base.BaseViewModel
import com.wisn.qm.mode.DataRepository
import com.wisn.qm.mode.beans.FileBean
import com.wisn.qm.mode.db.AppDataBase
import com.wisn.qm.mode.db.beans.MediaInfo
import com.wisn.qm.ui.view.ViewPosition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import java.io.File
import kotlin.collections.ArrayList

class SelectFileViewModel : BaseViewModel() {
    var filelistdata = MutableLiveData<MutableList<FileBean>>()
    var fileCurrentFileName = MutableLiveData<String>()
    var fileCurrentViewPosition = MutableLiveData<ViewPosition>()
    private var filePositionMap = HashMap<String, ViewPosition>()
    private var fileCurrentFile: File? = null
    private var fileRootName: String? = null

    var mediaImagelistdata = MutableLiveData<MutableList<MediaInfo>>()
    var mediaSelectList = MutableLiveData<ArrayList<MediaInfo>>()
    var mediaTitleShow = MutableLiveData<String>()

    fun getMediaImageList(selectList: MutableList<MediaInfo>?): MutableLiveData<MutableList<MediaInfo>> {

        LogUtils.d("getMediaImageList3AA ", Thread.currentThread().name)
        launchUI {
            launchFlow {
                LogUtils.d("getMediaImageList3 ", Thread.currentThread().name)
                val maxId = AppDataBase.getInstanse().mediaInfoDao?.getMediaInfoMaxId()
                val mediaImageList: MutableList<MediaInfo>?
                if (maxId != null && maxId > 0) {
                    val mediaImageListNew = DataRepository.getInstance().getMediaImageAndVideoList(maxId.toString())
                    mediaImageList = AppDataBase.getInstanse().mediaInfoDao?.getMediaInfoListAllNotDelete()
                    mediaImageListNew?.let {
                        if (mediaImageList != null && mediaImageList.isNotEmpty()) {
                            mediaImageList.addAll(mediaImageListNew)
                        }
                    }
                    mediaImageList?.sortByDescending {
                        it.createTime
                    }
                    mediaImageListNew?.let {
                        AppDataBase.getInstanse().mediaInfoDao?.insertMediaInfo(mediaImageListNew)
                    }
                } else {
                    mediaImageList = DataRepository.getInstance().getMediaImageAndVideoList("-1")
                    mediaImageList?.let {
                        mediaImageList?.sortByDescending {
                            it.createTime
                        }
                        AppDataBase.getInstanse().mediaInfoDao?.insertMediaInfo(mediaImageList)
                    }
                }
                //减小遍历次数
                var selectIdList = ArrayList<Long>()
                selectList?.let {
                    selectIdList.clear()
                    for (mediainfo in it) {
                        mediainfo.id?.let {
                            selectIdList.add(it)
                        }
                    }
                }
                //找出已经选择id的 mediainfo
                var tempposition = 0
                if (mediaImageList != null && selectList != null && selectList.size > 0) {
                    for (mediainfo in mediaImageList) {
                        if (selectIdList.contains(mediainfo.id)) {
                            mediainfo.isSelect = true
                            selectMediaData().value?.add(mediainfo)
                            tempposition++
                        }
                        //优化，不必所有的都遍历完
                        if (tempposition >= selectIdList.size) {
                            break
                        }
                    }
                }
                mediaImageList
            }.flowOn(Dispatchers.IO).collect {
                LogUtils.d("getMediaImageList3 BBB", Thread.currentThread().name)
                mediaImagelistdata.value = it
            }
        }
        return mediaImagelistdata
    }

    fun changeMediaSelectData(isAdd: Boolean, item: MediaInfo?) {
        if (item != null) {
            if (isAdd) {
                selectMediaData().value?.add(item)
            } else {
                selectMediaData().value?.remove(item)
            }
            selectMediaData().value = selectMediaData().value
        }
        mediaTitleShow.value = "已选中${selectMediaData().value?.size ?: 0}项"
    }


    fun selectMediaData(): MutableLiveData<ArrayList<MediaInfo>> {
        if (mediaSelectList.value == null) {
            mediaSelectList.value = ArrayList<MediaInfo>()
        }
        return mediaSelectList
    }

    fun setFileViewPosition(viewPosition: ViewPosition) {
        if (fileCurrentFile != null) {
            filePositionMap.put(fileCurrentFile!!.absolutePath, viewPosition)
        }
    }


    fun backFileList(): Boolean {
        if (fileCurrentFile == null || TextUtils.isEmpty(fileRootName)) {
            return true
        }
        if (fileRootName.equals(fileCurrentFile!!.absolutePath)) {
            return true
        }
        val parentFile = fileCurrentFile!!.parentFile
        if (parentFile.isDirectory) {
            getFileBeanList(parentFile)
        }
        return false
    }

    fun getFileBeanList(selectTargFile: File?): MutableLiveData<MutableList<FileBean>> {

        LogUtils.d("getFileBeanList ", Thread.currentThread().name)
        launchUI {
            launchFlow {
                var result: MutableList<FileBean> = ArrayList()
                LogUtils.d("getFileBeanList ", Thread.currentThread().name)
                if (selectTargFile != null) {
                    fileCurrentFile = selectTargFile
                    fileCurrentFile?.let {
                        fileCurrentFileName.postValue(it.name)
                    }
                } else {
                    fileCurrentFile = Environment.getExternalStorageDirectory()
                    fileRootName = fileCurrentFile?.absolutePath
                    fileCurrentFileName.postValue("/")
                }
                val listFiles = fileCurrentFile!!.listFiles()
                val iterator = listFiles.iterator();
                while (iterator.hasNext()) {
                    val next = iterator.next()
                    if (!next.isHidden()) {
                        result.add(FileBean(next.isDirectory, next.name, next.absolutePath, -1, next.length()))
                    }
                }
                result?.sortByDescending {
                    it.isDir
                }
//                result?.sorted()
//                result?.sortWith(Comparator { o1, o2 ->
//                    try {
//                        if (o1.isDir) {
//                            o1.fileName!!.compareTo(o2.fileName!!)
//                        } else {
//                            1
//                        }
//                    } catch (e: Exception) {
//                        1
//                    }
//                })
//                result?.sortWith(compareBy({it.fileName}))
//                result?.sortWith(compareBy({!it.isDir}))
//                result.sort()
                result
            }.flowOn(Dispatchers.IO).collect {
                LogUtils.d("getMediaImageList3 BBB", Thread.currentThread().name)
                filelistdata.value = it
                fileCurrentFile?.let {
                    val get = filePositionMap.get(it.absolutePath)
                    get?.let {
                        fileCurrentViewPosition.value = get

                    }
                }
            }
        }
        return filelistdata
    }
}