package com.wisn.qm.mode.beans


interface PreviewImage {
    val isLocal: Boolean
    val isThumbLocal: Boolean
    val resourcePath: String?
    val resourceThumbNailPath: String?
    val itemType: Int
}