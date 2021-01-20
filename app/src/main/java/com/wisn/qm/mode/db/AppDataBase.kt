package com.wisn.qm.mode.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.library.base.BaseApp
import com.wisn.qm.mode.db.beans.*
import com.wisn.qm.mode.db.dao.*

@Database(entities = [User::class, UploadBean::class, MediaInfo::class, UserDirBean::class, DiskUploadBean::class], version = 1)
abstract class AppDataBase : RoomDatabase() {
    abstract val userDao: UserDao?
    abstract val mediaInfoDao: MediaInfoDao?
    abstract val uploadBeanDao: UploadBeanDao?
    abstract val diskUploadBeanDao: DiskUploadBeanDao?
    abstract val userDirDao: UserDirDao?

    companion object {
        fun getInstanse() = SingletonHolder.wdata
    }

    private object SingletonHolder {
        val wdata: AppDataBase by lazy {
            Room.databaseBuilder(BaseApp.app, AppDataBase::class.java, "wdata")
//            .allowMainThreadQueries()
                    .build()
        }
    }



}