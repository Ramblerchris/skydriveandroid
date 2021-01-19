package com.wisn.qm.mode.beans

import android.text.TextUtils

/**
 *
 * @Description:
 * @Author: Wisn
 * @CreateDate: 2021/1/17 下午6:05
 */
data class FileBean(
        //是否是文件
        val isDir: Boolean,
        //文件名称
        val fileName: String?,
        //文件绝对路径
        val filePath: String?,
        //icon
        val resIcon: Int?,
        //大小
        val size: Long?) :Comparable<FileBean>{
    var __isSelect:Boolean=false

    override fun compareTo(other: FileBean): Int {
        if (isDir) {
            return 1
        }else{
//            if (!TextUtils.isEmpty(this.fileName) && !TextUtils.isEmpty(other.fileName)) {
//                return -1
//            }
//            //非文件
//            return 0
            return -1
        }

    }

}