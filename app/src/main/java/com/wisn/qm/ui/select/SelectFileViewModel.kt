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
import java.util.*
import kotlin.collections.ArrayList

class SelectFileViewModel : BaseViewModel() {
    var listdata = MutableLiveData<MutableList<MediaInfo>>()
    var listfilebean = MutableLiveData<MutableList<FileBean>>()
    var selectData = MutableLiveData<ArrayList<MediaInfo>>()
    var file: File? = null
    var rootName: String? = null

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
                listdata.value = it
            }
        }
        return listdata
    }

    fun selectData(): MutableLiveData<ArrayList<MediaInfo>> {
        if (selectData.value == null) {
            selectData.value = ArrayList<MediaInfo>()
        }
        return selectData
    }


    fun backFileList(): Boolean {
        if (file == null || TextUtils.isEmpty(rootName)) {
            return true
        }
        if (rootName.equals(file!!.absolutePath)) {
            return true
        }
        val parentFile = file!!.parentFile
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
                    file = selectTargFile
                } else {
                    file = Environment.getExternalStorageDirectory()
                    rootName = file?.absolutePath
                }
                val listFiles = file!!.listFiles()
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
                listfilebean.value = it
            }
        }
        return listfilebean
    }
}