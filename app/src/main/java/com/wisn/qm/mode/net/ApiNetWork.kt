package com.wisn.qm.mode.net

import com.library.base.config.UserBean
import com.library.base.net.RetrofitClient
import com.wisn.qm.mode.beans.BaseResult
import com.wisn.qm.mode.beans.MultiPartInfo
import com.wisn.qm.mode.beans.PageBean
import com.wisn.qm.mode.beans.Update
import com.wisn.qm.mode.db.beans.UserDirBean
import okhttp3.MultipartBody
import retrofit2.http.*

class ApiNetWork {

    var mService: Api? = null
//     var getServie() by lazy { RetrofitClient.getInstance().create(Api::class.java) }

    suspend fun login(username: String, password: String): BaseResult<String> {
        return getServie().login(username, password)
    }

    fun getServie(): Api {
        if (mService == null) {
            synchronized(this) {
                if (mService == null) {
                    mService = RetrofitClient.getInstance().create(Api::class.java)
                }
            }
        }
        return mService!!
    }

    fun updateBaseUrl(ip: String) {
        val instance = RetrofitClient.getInstance();
        instance.updateBaseUrl(ip)
        mService = instance.create(Api::class.java)
    }


    suspend fun register(username: String, password: String, password1: String): BaseResult<UserBean> {
        return getServie().register(username, password, password1)
    }

    /**
     * 用户信息
     */
    suspend fun getUserInfo(): BaseResult<UserBean> {
        return getServie().getUserInfo()
    }

    /**
     * 用户退出
     */
    suspend fun singout(): BaseResult<String> {
        return getServie().singout()
    }

    /**
     * 单个文件信息
     */
    suspend fun getFileInfo(sha1: Long): BaseResult<UserDirBean> {
        return getServie().getFileInfo(sha1)
    }


    /**
     * 删除单个文件
     */
    suspend fun deleteFile(sha1: String): BaseResult<String> {
        return getServie().deleteFile(sha1)
    }

    /**
     * 所有文件sha1
     */
    suspend fun getAllSha1sByUser(): BaseResult<MutableList<String>> {
        return getServie().getAllSha1sByUser()
    }

    /**
     * 当前用户所有文件夹
     */
    suspend fun getUserFileAlllist(): BaseResult<PageBean<List<UserDirBean>>> {
        return getServie().getUserFileAlllist()
    }

    /**
     * 单文件上传
     */
    suspend fun uploadFile(sha1: String, pid: Long, isVideo: Boolean, minetype: String, videoduration: Long, file: MultipartBody.Part): BaseResult<UserDirBean> {
        return getServie().uploadFile(sha1, pid, isVideo, minetype, videoduration, file)
    }


    suspend fun deletefiles(pid: Long, sha1s: String): BaseResult<Any> {
        return getServie().deletefiles(pid, sha1s)
    }

    /**
     *更改用户头像
     */
    suspend fun updateUserPhoto(file: MultipartBody.Part): BaseResult<UserDirBean> {
        return getServie().updateUserPhoto(file)
    }

    /**
     * 修改用户名
     */
    suspend fun updateUserName(filename: String): BaseResult<Boolean> {
        return getServie().updateUserName(filename)
    }


    /**
     * 单文件上传，秒传
     */
    suspend fun uploadFileHitpass(pid: Long, sha1: String): BaseResult<UserDirBean> {
        return getServie().uploadFileHitpass(pid, sha1)
    }


    /**
     * 每个目录的文件夹列表
     */
    suspend fun getUserDirlist(pid: Long,  pageSize:Long? =20,lastId: Long? =-1): BaseResult<PageBean<MutableList<UserDirBean>>> {
        return getServie().getUserDirlist(pid,pageSize,lastId)
    }


    /**
     * 添加文件夹
     */
    suspend fun addUserDir( pid: Long,  filename: String): BaseResult<UserDirBean> {
        return getServie().addUserDir(pid, filename)
    }

    /**
     * 初始化分块上传
     */
    suspend fun initMultipartInfo(pid: Long,  filename: String,
                                   filesize: Long,sha1: String): BaseResult<MultiPartInfo> {
        return getServie().initMultipartInfo(pid, filename, filesize, sha1)
    }

    /**
     * 通知分块上传完成
     */
    suspend fun finishMultipartInfo( uploadId: String, sha1: String): BaseResult<MultiPartInfo> {
        return getServie().finishMultipartInfo(uploadId, sha1)
    }

    /**
     * 分块上传
     */
    suspend fun uploadMultipartInfo( pid: Long, uploadId: String, chunkindex: Int): BaseResult<MultiPartInfo> {
        return getServie().uploadMultipartInfo(pid, uploadId, chunkindex)
    }

    /**
     * 删除文件夹
     */
    suspend fun deleteDirs(@Query("ids") ids: String): BaseResult<Boolean> {
        return getServie().deleteDirs(ids)
    }

    /**
     * 蒲公英更新
     */
    suspend fun checkUpdate(buildVersion: String, buildBuildVersion: String): BaseResult<Update> {
        return getServie().checkUpdate(buildVersion = buildVersion, buildBuildVersion = buildBuildVersion)
    }


    /**
     * 单文件上传，秒传
     */
    suspend fun uploadDiskFileHitpass(pid: Long, sha1: String): BaseResult<UserDirBean> {
        return getServie().uploadDiskFileHitpass(pid, sha1)
    }

    /**
     * 删除单个文件
     */
    suspend fun deleteDiskFile(sha1: String): BaseResult<String> {
        return getServie().deleteDiskFile(sha1)
    }

    /**
     * 获取每个disk目录的文件夹列表
     */
    suspend fun getDiskDirlist(pid: Long, pageSize: Long? = 20, lastId: Long? = -1): BaseResult<PageBean<MutableList<UserDirBean>>> {
        return getServie().getDiskDirlist(pid, pageSize, lastId)
    }

    /**
     * 添加文件夹
     */
    suspend fun addDiskDir(pid: Long, filename: String): BaseResult<UserDirBean> {
        return getServie().uploadDiskFileHitpass(pid, filename)
    }


    companion object {
        private var netWork: ApiNetWork? = null
        fun newInstance() = netWork ?: synchronized(this) {
            netWork ?: ApiNetWork().also { netWork = it }
        }
    }
}