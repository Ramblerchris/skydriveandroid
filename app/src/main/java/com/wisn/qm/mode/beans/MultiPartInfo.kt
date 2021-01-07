package com.wisn.qm.mode.beans

data class MultiPartInfo(
    val chunkcount: Int,
    val chunksize: Int,
    val filename: String,
    val pid: Int,
    val sha1: String,
    val size: Int,
    val uploadid: String
)