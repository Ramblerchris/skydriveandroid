package com.wisn.qm.mode.db.beans

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.google.gson.annotations.SerializedName
import com.library.base.utils.FormatStrUtils
import com.wisn.qm.mode.beans.FileType

@Entity(tableName = "uploadalbum")
data class UploadBean(
        @ColumnInfo(name = "mediainfoid")
        @SerializedName("mediainfoid")
        var mediainfoid: Long?,

        @ColumnInfo(name = "filename")
        @SerializedName("fileName")
        var fileName: String?,


        @ColumnInfo(name = "filepath")
        @SerializedName("filePath")
        var filePath: String?,

        @ColumnInfo(name = "filesize")
        @SerializedName("fileSize")
        var fileSize: Long?,

        @ColumnInfo(name = "mimetype")
        @SerializedName("mimeType")
        var mimeType: String?,

        @ColumnInfo(name = "createtime")
        @SerializedName("createTime")
        var createTime: Long?,

        @ColumnInfo(name = "pid")
        var pid: Long = 0,

        @ColumnInfo(name = "uploadStatus")
        var uploadStatus: Int = 0,

        @ColumnInfo(name = "sha1")
        var sha1: String? = "",

        @ColumnInfo(name = "isvideo")
        @SerializedName("isvideo")
        var isVideo: Boolean?,


        @ColumnInfo(name = "duration")
        @SerializedName("duration")
        var duration: Long?
) : MultiItemEntity {
    @ColumnInfo(name = "filesizeStr")
    @SerializedName("filesizeStr")
    var filesizeStr: String="";

    @ColumnInfo(name = "upDirName")
    @SerializedName("upDirName")
    var upDirName: String?=""

    //是否是秒传
    @ColumnInfo(name = "isHitPass")
    @SerializedName("isHitPass")
    var isHitPass: Boolean?=false

    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    var id: Long = 0;

    @ColumnInfo(name = "uploadSuccessTime")
    @SerializedName("uploadSuccessTime")
    var uploadSuccessTime: Long? = 0

    @Ignore
    var uploadSuccessTimeStr: String? = null

    @Ignore
    override var itemType: Int = 0
        get() {
            if(mediainfoid!=null&& mediainfoid!! >=0){
                return FileType.UploadInfoProgressItem
            }
            return field
        }
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as UploadBean
        if (filePath != other.filePath) return false
        if (pid != other.pid) return false
        if (sha1 != other.sha1) return false

        return true
    }

    override fun hashCode(): Int {
        var result = filePath?.hashCode() ?: 0
        result = 31 * result + pid.hashCode()
        result = 31 * result + (sha1?.hashCode() ?: 0)
        return result
    }

    fun getUploadStatusStr(): String {
        if (uploadStatus == FileType.UPloadStatus_uploadSuccess) {
            return "已上传"
        } else if (uploadStatus == FileType.UPloadStatus_Noupload) {
            return "未上传"
        } else if (uploadStatus == FileType.UPloadStatus_uploading) {
            return "上传中"
        } else if (uploadStatus == FileType.UPloadStatus_uploadDelete) {
            return "本地文件已经删除"
        }
        return "未上传"
    }

    fun getUploadTimeStr(): String {
        if(uploadSuccessTime!! <=0){
            return ""
        }
        if(uploadSuccessTimeStr.isNullOrEmpty()){
            uploadSuccessTimeStr=FormatStrUtils.getformatDate(uploadSuccessTime!!);
        }
        return uploadSuccessTimeStr!!

    }

    fun getStatusStr(): String {
        if(uploadStatus == FileType.UPloadStatus_uploadSuccess){
            return "${getUploadTimeStr()} ${getUploadStatusStr()}"
        }else{
            return getUploadStatusStr()
        }
    }

    override fun toString(): String {
        return "UploadBean(fileName=$fileName, filePath=$filePath, fileSize=$fileSize, mimeType=$mimeType, createTime=$createTime, pid=$pid, uploadStatus=$uploadStatus, sha1=$sha1, id=$id)"
    }


}