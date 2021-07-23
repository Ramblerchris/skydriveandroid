package com.wisn.qm.mode.beans


interface PreviewImage {
    val isLocal: Boolean
    val isThumbLocal: Boolean
    val resourcePath: String?
    fun getResourcePathOrigin(): String?{
        return ""
    }

    val resourceThumbNailPath: String?
    val itemType: Int
}