package com.wisn.qm.mode.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wisn.qm.mode.db.beans.DiskUploadBean

@Dao
interface DiskUploadBeanDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiskUploadBeanList(uploadBeanlist: List<DiskUploadBean>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiskUploadBean(uploadBean: DiskUploadBean)

    @Query("select * from diskuploadbean where uploadStatus =:uploadStatus")
    suspend fun getDiskUploadBeanListPreUpload(uploadStatus: Int): List<DiskUploadBean>

    @Query("update diskuploadbean set uploadStatus =:uploadStatus,uploadSuccessTime=:uploadSuccessTime where id=:id ")
    suspend fun updateDiskUploadBeanStatus(id: Long, uploadStatus: Int,uploadSuccessTime: Long)

    @Query("update diskuploadbean set sha1 =:sha1 where id=:id ")
    suspend fun updateDiskUploadBeanSha1(id: Long, sha1: String)

    @Query("select * from diskuploadbean order by id desc")
    suspend fun getDiskUploadBeanListAll(): MutableList<DiskUploadBean>


}