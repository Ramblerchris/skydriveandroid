package com.wisn.qm.mode.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wisn.qm.mode.db.beans.UploadBean

@Dao
interface UploadBeanDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUploadBeanList(uploadBeanlist: List<UploadBean>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUploadBean(uploadBean: UploadBean)

    @Query("select * from uploadalbum where  pid= :pid")
    suspend fun getUploadBeanList(pid: Long): List<UploadBean>

    @Query("select * from uploadalbum where uploadStatus =:uploadStatus")
    suspend fun getUploadBeanListPreUpload(uploadStatus: Int): List<UploadBean>

    @Query("select count(*) from uploadalbum where uploadStatus =:uploadStatus")
    suspend fun getCountByStatus(uploadStatus: Int): Int

    @Query("update uploadalbum set uploadStatus =:uploadStatus,uploadSuccessTime=:uploadSuccessTime,isHitPass=:isHitPass where id=:id ")
    suspend fun updateUploadBeanStatus(id: Long, uploadStatus: Int,uploadSuccessTime: Long,isHitPass: Boolean=false)

    @Query("update uploadalbum set uploadStatus =:newuploadStatus where uploadStatus=:oldUploadStatus ")
    suspend fun updateUploadBeanStatusByStatus( oldUploadStatus: Int,newuploadStatus: Int)

    @Query("select * from uploadalbum order by id desc")
    suspend fun getUploadBeanListAll(): MutableList<UploadBean>

    @Query("select * from uploadalbum  where id=:id ")
    suspend fun getUploadBeanById(id: Long): UploadBean


}