package com.wisn.qm.ui.select

import android.os.Environment
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

class SelectFileViewModel : BaseViewModel() {
    var listdata = MutableLiveData<MutableList<MediaInfo>>()
    var listfilebean = MutableLiveData<MutableList<FileBean>>()
    var selectData = MutableLiveData<ArrayList<MediaInfo>>()

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

    fun getFileBeanList(selectTargFile: File?): MutableLiveData<MutableList<FileBean>> {

        LogUtils.d("getFileBeanList ", Thread.currentThread().name)
        launchUI {
            launchFlow {
                var result: MutableList<FileBean> = ArrayList()
                LogUtils.d("getFileBeanList ", Thread.currentThread().name)
                var file: File
                if (selectTargFile != null) {
                    file = selectTargFile
                } else {
                    file = Environment.getExternalStorageDirectory()
                }
                val listFiles = file!!.listFiles()
                val iterator = listFiles.iterator();
                while (iterator.hasNext()) {
                    val next = iterator.next();
                    result.add(FileBean(next.isDirectory, next.name, next.absolutePath, -1, next.length()))

                }
                result
            }.flowOn(Dispatchers.IO).collect {
                LogUtils.d("getMediaImageList3 BBB", Thread.currentThread().name)
                listfilebean.value = it
            }
        }
        return listfilebean
    }
}