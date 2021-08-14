package com.wisn.qm.mode.beans

import com.library.base.utils.FormatStrUtils


interface PreviewImage {
    val isLocal: Boolean
    val isThumbLocal: Boolean
    val resourcePath: String?
    fun getResourcePathOrigin(): String?{
        return ""
    }

    val resourceThumbNailPath: String?
    val itemType: Int
    val resourceSize: Long?

    fun getResourceSizeStr(): String?{
        resourceSize?.let {
            return FormatStrUtils.getFormatDiskSizeStr(it)
        }
        return ""
    }
   /* val resourceSizeStr: String?
    fun getResourceSizeStr(): String?{
        return ""
    }*/
}