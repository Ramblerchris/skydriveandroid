package com.wisn.qm.mode.beans

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
        val size: Long?) {

}