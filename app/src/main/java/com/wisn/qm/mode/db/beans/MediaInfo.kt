package com.wisn.qm.mode.db.beans

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.google.gson.annotations.SerializedName
import com.wisn.qm.mode.beans.FileType
import com.wisn.qm.mode.beans.PreviewImage
import java.io.Serializable

@Entity(tableName = "mediainfo")
data class MediaInfo(

        @PrimaryKey
        @SerializedName("id")
        var id: Long?,

        @ColumnInfo(name = "filename")
        @SerializedName("fileName")
        var fileName: String?,

        @ColumnInfo(name = "filepath")
        @SerializedName("filePath")
        var filePath: String?,

        @ColumnInfo(name = "filesize")
        @SerializedName("fileSize")
        var fileSize: Long?,

        @ColumnInfo(name = "duration")
        @SerializedName("duration")
        var duration: Long?,

        @ColumnInfo(name = "mimetype")
        @SerializedName("mimeType")
        var mimeType: String?,

        @ColumnInfo(name = "isvideo")
        @SerializedName("isvideo")
        var isVideo: Boolean?,

        @ColumnInfo(name = "createtime")
        @SerializedName("createTime")
        var createTime: Long?,

        @ColumnInfo(name = "thumbnailpath")
        @SerializedName("thumbNailPath")
        var thumbNailPath: String?,

        @ColumnInfo(name = "latitude")
        @SerializedName("latitude")
        var latitude: Float?,

        @ColumnInfo(name = "longitude")
        @SerializedName("longitude")
        var longitude: Float?,

        @ColumnInfo(name = "width")
        @SerializedName("width")
        var width: Int?,

        @ColumnInfo(name = "height")
        @SerializedName("height")
        var height: Int?


) :Serializable, MultiItemEntity , PreviewImage {

    override val isLocal: Boolean
        get() = true
    override val isThumbLocal: Boolean
        get() = true
    override val resourcePath: String?
        get() = this.filePath
    override val resourceThumbNailPath: String?
        get() = this.thumbNailPath

    override var itemType: Int = 0
    get() {
        if (isVideo!!) {
            return FileType.VideoViewItem
        }
        if (field == 0) {
            return FileType.ImageViewItem
        }
        return field
    }

    @Ignore
    constructor (itemType: Int):this(0,null,null,0,0,null,false,0,null,0f,0f,0,0) {
        this.itemType=itemType
    }

    @ColumnInfo(name = "pid")
    @SerializedName("pid")
    var pid: Long = 0

    @ColumnInfo(name = "uploadStatus")
    @SerializedName("uploadStatus")
    var uploadStatus: Int = 0

    @ColumnInfo(name = "sha1")
    @SerializedName("sha1")
    var sha1: String? = ""

    @ColumnInfo(name = "timestr")
    @SerializedName("timestr")
    var timestr: String? = ""

    @Ignore
    var isSelect: Boolean = false
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MediaInfo

        if (id != other.id) return false
        if (fileName != other.fileName) return false
        if (filePath != other.filePath) return false
        if (fileSize != other.fileSize) return false
        if (duration != other.duration) return false
        if (mimeType != other.mimeType) return false
        if (createTime != other.createTime) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (fileName?.hashCode() ?: 0)
        result = 31 * result + (filePath?.hashCode() ?: 0)
        result = 31 * result + (fileSize?.hashCode() ?: 0)
        result = 31 * result + (duration?.hashCode() ?: 0)
        result = 31 * result + (mimeType?.hashCode() ?: 0)
        result = 31 * result + (createTime?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "MediaInfo(id=$id, fileName=$fileName, filePath=$filePath, fileSize=$fileSize, pid=$pid, uploadStatus=$uploadStatus, sha1=$sha1)"
    }


}