package com.wisn.qm.mode.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.blankj.utilcode.util.LogUtils
import com.library.base.BaseApp
import com.wisn.qm.mode.db.beans.*
import com.wisn.qm.mode.db.dao.*


@Database(entities = [User::class, UploadBean::class, MediaInfo::class, UserDirBean::class, DiskUploadBean::class], exportSchema = false,version =3)
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
            //版本号
            val MIGRATION_app1_2: Migration = object : Migration(1, 2) {
                override fun migrate(database: SupportSQLiteDatabase) {
//            //没有布尔值，用INTEGER代替
//            //使用"ALTER TABLE Word  ADD COLUMN bar_data2 INTEGER  NOT NULL DEFAULT 1"出错。
//            //使用下面分开的形式,可以正确执行
//                    database.execSQL("ALTER TABLE userdirlist "+"   ADD COLUMN isShare "+" INTEGER NOT NULL DEFAULT 0 ")
//                    database.execSQL("ALTER TABLE userdirlist "+"   ADD COLUMN isShareFromMe "+" INTEGER  NOT NULL DEFAULT 0 ")
//                    database.execSQL("ALTER TABLE userdirlist "+"   ADD COLUMN ShareFrom TEXT "+" NOT NULL DEFAULT ")
                LogUtils.d("MIGRATION_app1_2")
                    database.execSQL("ALTER TABLE userdirlist "+"   ADD COLUMN isShare INTEGER  ")
                    database.execSQL("ALTER TABLE userdirlist "+"   ADD COLUMN isShareFromMe  INTEGER    ")
                    database.execSQL("ALTER TABLE userdirlist "+"   ADD COLUMN ShareFrom    TEXT  ")
                }
            }
            Room.databaseBuilder(BaseApp.app, AppDataBase::class.java, "skydriver13")
//            .allowMainThreadQueries()
                    .addMigrations(MIGRATION_app1_2)
                    .build()
        }
    }



}