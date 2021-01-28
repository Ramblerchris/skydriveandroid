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
    var mediaImagelistdata = MutableLiveData<MutableList<MediaInfo>>()
    var filelistdata = MutableLiveData<MutableList<FileBean>>()
    var mediaSelectList = MutableLiveData<ArrayList<MediaInfo>>()
    var currentFileName = MutableLiveData<String>()
    var currentViewPosition = MutableLiveData<ViewPosition>()
    private var positionMap = HashMap<String,ViewPosition>()
    private var currentFile: File? = null
    private var rootName: String? = null

    private var selectIdList = ArrayList<Long>()

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
                selectList?.let {
                    selectIdList.clear()
                    for (mediainfo in it) {
                        mediainfo.id?.let {
                            selectIdList.add(it)
                        }
                    }
                }
                var tempposition = 0
                if (mediaImageList != null && selectList != null && selectList.size > 0) {
                    for (mediainfo in mediaImageList) {
                        if (selectIdList.contains(mediainfo.id)) {
                            mediainfo.isSelect = true
                            selectData().value?.add(mediainfo)
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

    fun selectData(): MutableLiveData<ArrayList<MediaInfo>> {
        if (mediaSelectList.value == null) {
            mediaSelectList.value = ArrayList<MediaInfo>()
        }
        return mediaSelectList
    }

    fun setViewPosition(viewPosition: ViewPosition) {
        if (currentFile != null) {
            positionMap.put(currentFile!!.absolutePath,viewPosition)
        }
    }


    fun backFileList(): Boolean {
        if (currentFile == null || TextUtils.isEmpty(rootName)) {
            return true
        }
        if (rootName.equals(currentFile!!.absolutePath)) {
            return true
        }
        val parentFile = currentFile!!.parentFile
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
                    currentFile = selectTargFile
                    currentFile?.let {
                        currentFileName.postValue(it.name)
                    }
                } else {
                    currentFile = Environment.getExternalStorageDirectory()
                    rootName = currentFile?.absolutePath
                    currentFileName.postValue("/")
                }
                val listFiles = currentFile!!.listFiles()
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
                currentFile?.let {
                    val get = positionMap.get(it.absolutePath)
                    get?.let {
                        currentViewPosition.value=get

                    }
                }
            }
        }
        return filelistdata
    }
}