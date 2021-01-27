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
    private var currentFile: File? = null
    private var rootName: String? = null

    fun getMediaImageList(): MutableLiveData<MutableList<MediaInfo>> {

        LogUtils.d("getMediaImageList3AA ", Thread.currentThread().name)
        launchUI {
            launchFlow {
                LogUtils.d("getMediaImageList3 ", Thread.currentThread().name)
                val maxId = AppDataBase.getInstanse().mediaInfoDao?.getMediaInfoMaxId();
                if (maxId != null && maxId > 0) {
                    val mediaImageListNew = DataRepository.getInstance().getMediaImageAndVideoList(maxId.toString())
                    val mediaInfoListAll = AppDataBase.getInstanse().mediaInfoDao?.getMediaInfoListAllNotDelete()
                    mediaImageListNew?.let {
                        if (mediaInfoListAll != null && mediaInfoListAll.isNotEmpty()) {
                            mediaInfoListAll.addAll(mediaImageListNew)
                        }
                    }
                    mediaInfoListAll?.sortByDescending {
                        it.createTime
                    }
                    mediaImageListNew?.let {
                        AppDataBase.getInstanse().mediaInfoDao?.insertMediaInfo(mediaImageListNew)
                    }
                    mediaInfoListAll;
                } else {
                    val mediaImageList = DataRepository.getInstance().getMediaImageAndVideoList("-1")
                    mediaImageList?.let {
                        mediaImageList?.sortByDescending {
                            it.createTime
                        }
                        AppDataBase.getInstanse().mediaInfoDao?.insertMediaInfo(mediaImageList)
                    }
                    mediaImageList;
                }
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
            }
        }
        return filelistdata
    }
}