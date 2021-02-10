package com.wisn.qm.mode.net

import com.wisn.qm.mode.beans.BaseResult
import com.library.base.config.UserBean
import com.wisn.qm.mode.ConstantKey
import com.wisn.qm.mode.beans.MultiPartInfo
import com.wisn.qm.mode.beans.PageBean
import com.wisn.qm.mode.beans.Update
import com.wisn.qm.mode.db.beans.UserDirBean
import okhttp3.MultipartBody
import retrofit2.http.*

interface Api {
    /************************************用户模块******************************************/

    /**
     *
     */
    @FormUrlEncoded
    @POST("/user/signin")
    suspend fun login(@Field("phone") username: String, @Field("password") password: String): BaseResult<String>


    @FormUrlEncoded
    @POST("/user/register")
    suspend fun register(@Field("phone") username: String, @Field("password") password: String, @Field("password2") password2: String): BaseResult<UserBean>

    /**
     * 用户信息
     */
    @GET("/user/getuserinfo")
    suspend fun getUserInfo(): BaseResult<UserBean>

    /**
     * 用户退出
     */
    @GET("/user/signout")
    suspend fun singout(): BaseResult<String>

    /**
     * 修改用户头像
     */
    @Multipart
    @POST("/user/updatePhoto")
    suspend fun updateUserPhoto(@Part file: MultipartBody.Part): BaseResult<UserDirBean>


    /**
     * 修改用户名
     */
    @FormUrlEncoded
    @POST("/user/updateUserName")
    suspend fun updateUserName(@Field("name") filename: String): BaseResult<Boolean>

    /************************************文件******************************************/

    /**
     * 单个文件信息
     */
    @GET("/file/getinfo")
    suspend fun getFileInfo(@Query("sha1") sha1: Long): BaseResult<UserDirBean>


    /**
     * 删除单个文件
     */
    @DELETE("/file/delete")
    suspend fun deleteFile(@Query("filesha1") sha1: String): BaseResult<String>

    /************************************用户文件******************************************/

    /**
     * 删除批量文件
     * 10e74a3cab06913f4f6858d5ed91ca708d90b41a;0f50dc07dfd02628dff82ed8873f7a06dad80525
     *
     */
    @POST("/userfile/deletefiles")
    suspend fun deleteUserfilesByPidAndSha1s(@Query("pid") pid: Long, @Query("sha1s") sha1s: String): BaseResult<Any>

    /**
     * 获取当前用户所有文件夹
     */
    @GET("/userfile/getlist")
    suspend fun getUserFileAlllist(): BaseResult<PageBean<List<UserDirBean>>>

    /**
     * 单文件上传
     */
    @Multipart
    @POST("/userfile/upload")
    suspend fun uploadFile(@Part("sha1") sha1: String, @Part("pid") pid: Long, @Part("isVideo") isVideo: Boolean, @Part("minetype") minetype: String, @Part("videoduration") videoduration: Long, @Part file: MultipartBody.Part): BaseResult<UserDirBean>

    /**
     * 单文件上传，秒传
     */
    @POST("/userfile/hitpass")
    suspend fun uploadFileHitpass(@Query("pid") pid: Long, @Query("sha1") sha1: String): BaseResult<UserDirBean>

    /**
     * 修改单个文件夹名称
     */
    @GET("/userfile/updateName")
    suspend fun updateUserDirName(@Query("id") id: Long,@Query("newfilename") sha1: String): BaseResult<String>


    /**
     * 获取每个目录的文件夹列表
     */
    @GET("/userfile/dirlist")
    suspend fun getUserDirlist(@Query("pid") pid: Long, @Query("pageSize") pageSize: Long? = 20, @Query("lastId") lastId: Long? = -1): BaseResult<PageBean<MutableList<UserDirBean>>>

    /**
     * 获取所有文件sha1
     */
    @GET("/userfile/getAllSha1sByUser")
    suspend fun getAllSha1sByUser(): BaseResult<MutableList<String>>


    /**
     * 添加文件夹
     */
    @FormUrlEncoded
    @POST("/userfile/adddir")
    suspend fun addUserDir(@Field("pid") pid: Long, @Field("filename") filename: String): BaseResult<UserDirBean>


    /**
     * 删除文件夹
     * ids=5;17;18;
     */
    @POST("/userfile/deleteDir")
    suspend fun deleteDirs(@Query("ids") ids: String): BaseResult<Boolean>

    /***********************************蒲公英更新*******************************************/

    /**
     * 蒲公英更新
     */
    @FormUrlEncoded
    @POST
    suspend fun checkUpdate(@Url url: String = ConstantKey.pgyerUpdate, @Field("_api_key") _api_key: String = ConstantKey._api_key, @Field("appKey") appKey: String = ConstantKey.appKey,
                            @Field("buildVersion") buildVersion: String, @Field("buildBuildVersion") buildBuildVersion: String): BaseResult<Update>

    /************************************disk******************************************/

    /**
     * 单文件上传，秒传
     */
    @POST("/disk/hitpass")
    suspend fun uploadDiskFileHitpass(@Query("pid") pid: Long, @Query("sha1") sha1: String): BaseResult<UserDirBean>

    /**
     * 删除单个文件
     */
    @POST("/disk/delete")
    suspend fun deleteDiskFile(@Query("ids") ids: String): BaseResult<String>


    /**
     * 获取每个disk目录的文件夹列表
     */
    @GET("/disk/dirlist")
    suspend fun getDiskDirlist(@Query("pid") pid: Long, @Query("pageSize") pageSize: Long? = 20, @Query("lastId") lastId: Long? = -1): BaseResult<PageBean<MutableList<UserDirBean>>>

    /**
     * 添加文件夹
     */
    @FormUrlEncoded
    @POST("/disk/adddir")
    suspend fun addDiskDir(@Field("pid") pid: Long, @Field("filename") filename: String): BaseResult<UserDirBean>

    /**
     * 单文件上传
     */
    @Multipart
    @POST("/disk/upload")
    suspend fun uploadDiskFile(@Part("sha1") sha1: String, @Part("pid") pid: Long, @Part("minetype") minetype: String,  @Part file: MultipartBody.Part): BaseResult<UserDirBean>


    /**
     * 修改单个文件夹名称
     */
    @GET("/disk/updateName")
    suspend fun updateDiskDirName(@Query("id") id: Long,@Query("newfilename") sha1: String): BaseResult<String>

    /**
     * 控制服务器开机和关机
     */
    @GET("/admin/sdrb")
    suspend fun adminSDRB(@Query("type") type: String,@Query("time") time: String?): BaseResult<String>

    /************************************分块上传******************************************/
    /**
     * 初始化分块上传
     */
    @GET("/userfile/initmultipartinfo")
    suspend fun initMultipartInfo(@Query("pid") pid: Long, @Query("filename") filename: String,
                                  @Query("filesize") filesize: Long, @Query("sha1") sha1: String): BaseResult<MultiPartInfo>

    /**
     * 通知分块上传完成
     */
    @GET("/userfile/finishmultipartinfo")
    suspend fun finishMultipartInfo(@Query("uploadId") uploadId: String, @Query("sha1") sha1: String): BaseResult<MultiPartInfo>

    /**
     * 分块上传
     */
    @POST("/userfile/uploadmultipartinfo")
    suspend fun uploadMultipartInfo(@Query("pid") pid: Long, @Query("uploadId") uploadId: String, @Query("chunkindex") chunkindex: Int): BaseResult<MultiPartInfo>


}