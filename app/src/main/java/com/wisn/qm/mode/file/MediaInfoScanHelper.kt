package com.wisn.qm.mode.file

import android.database.Cursor
import android.provider.MediaStore
import android.text.TextUtils
import com.blankj.utilcode.util.LogUtils
import com.library.base.BaseApp
import com.library.base.utils.FormatStrUtils
import com.library.base.utils.SHAMD5Utils
import com.wisn.qm.mode.db.AppDataBase
import com.wisn.qm.mode.db.beans.Folder
import com.wisn.qm.mode.db.beans.MediaInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class MediaInfoScanHelper {
    val mediaIamgeArrayof = arrayOf(
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.DATA,
        MediaStore.Images.Media.WIDTH,
        MediaStore.Images.Media.HEIGHT,
        MediaStore.Images.Media.ORIENTATION,
        MediaStore.Images.Media.DISPLAY_NAME,
        MediaStore.Images.Media.DATE_ADDED,
        MediaStore.Images.Media.SIZE,
        MediaStore.Images.Media.MIME_TYPE,
        MediaStore.Images.Media.LATITUDE,
        MediaStore.Images.Media.LONGITUDE
    )
    val mediaVideoArrayof = arrayOf(
        MediaStore.Video.Media._ID,
        MediaStore.Video.Media.DATA,
        MediaStore.Video.Media.WIDTH,
        MediaStore.Video.Media.HEIGHT,
//                            MediaStore.Video.Media.ORIENTATION,
        MediaStore.Video.Media.DISPLAY_NAME,
        MediaStore.Video.Media.DATE_ADDED,
        MediaStore.Video.Media.SIZE,
        MediaStore.Video.Media.DURATION,
        MediaStore.Video.Media.MIME_TYPE,
        MediaStore.Video.Media.LATITUDE,
        MediaStore.Video.Media.LONGITUDE
    )

    suspend fun getMediaImageList(maxid: String): MutableList<MediaInfo> {
        return withContext(Dispatchers.IO) {
            val query = BaseApp.app.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                mediaIamgeArrayof,
                MediaStore.Images.Media._ID + ">=?",
                arrayOf(maxid),
                null
//                MediaStore.Images.Media.DATE_ADDED + " desc"
            )
            var result = getImageListByQuery(query,true)
            result
        }
    }

    suspend fun getMediaVideoList(maxid: String): MutableList<MediaInfo> {
        return withContext(Dispatchers.IO) {
            var videoMediaInfoMaxId =
                AppDataBase.getInstanse().mediaInfoDao?.getVideoMediaInfoMaxId();
            if (videoMediaInfoMaxId == null) {
                videoMediaInfoMaxId = 0
            }

            val query = BaseApp.app.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                mediaVideoArrayof,
//                    MediaStore.Video.Media._ID + " > ? and( "
//                    MediaStore.Video.Media._ID + " > "+videoMediaInfoMaxId+" and( "
//                            + MediaStore.Video.Media.MIME_TYPE + "=? or "
//                            + MediaStore.Video.Media.MIME_TYPE + "=? or "
//                            + MediaStore.Video.Media.MIME_TYPE + "=? or "
//                            + MediaStore.Video.Media.MIME_TYPE + "=? or "
//                            + MediaStore.Video.Media.MIME_TYPE + "=? or "
//                            + MediaStore.Video.Media.MIME_TYPE + "=? or "
//                            + MediaStore.Video.Media.MIME_TYPE + "=? or "
//                            + MediaStore.Video.Media.MIME_TYPE + "=? or "
//                            + MediaStore.Video.Media.MIME_TYPE + "=?) ",
//
////                    arrayOf(videoMediaInfoMaxId.toString(), "video/mp4", "video/3gp", "video/aiv", "video/rmvb", "video/vob", "video/flv",
//                    arrayOf( "video/mp4", "video/3gp", "video/aiv", "video/rmvb", "video/vob", "video/flv",
//                            "video/mkv", "video/mov", "video/mpg"),
//                    MediaStore.Video.Media._ID + " > ? and( "
                MediaStore.Video.Media._ID + " >=? ",

//                    arrayOf(videoMediaInfoMaxId.toString(), "video/mp4", "video/3gp", "video/aiv", "video/rmvb", "video/vob", "video/flv",
                arrayOf(videoMediaInfoMaxId.toString()),
                MediaStore.Video.Media.DATE_ADDED
//                MediaStore.Images.Media.DATE_ADDED + " desc"
            )
            var result = getVideoListByQuery(query,true)
            result
        }
    }


    open suspend fun getMediaImageVidoeListNoSha1(addVideo: Boolean): ArrayList<MediaInfo> {
        return withContext(Dispatchers.IO) {
            val mediaImageListNoSha1 = getMediaImageListNoSha1()
            if(addVideo){
                val mediaVideoListNoSha1 = getMediaVideoListNoSha1()
                mediaImageListNoSha1.addAll(mediaVideoListNoSha1)
            }
            mediaImageListNoSha1
        }
    }

    open suspend fun getMediaImageVidoeFolderListNoSha1(addVideo: Boolean): ArrayList<Folder> {
        return withContext(Dispatchers.IO) {
            val mediaImageListNoSha1 = getMediaImageListNoSha1()
            if(addVideo){
                val mediaVideoListNoSha1 = getMediaVideoListNoSha1()
                mediaImageListNoSha1.addAll(mediaVideoListNoSha1)
            }
            getFolderByMediaInfoList(mediaImageListNoSha1)
        }
    }

    /**
     * 获取相册，并排序
     */
    open fun getFolderByMediaInfoList(mediaImageListNoSha1: ArrayList<MediaInfo>): ArrayList<Folder> {
        val splitFolder = splitFolder("全部相册", mediaImageListNoSha1)
        //按照时间排序
        for (i in splitFolder.indices) {
            Collections.sort(splitFolder.get(i).images, kotlin.Comparator { o1, o2 ->
                return@Comparator when {
                    o1.createTime == o2.createTime -> {
                        0
                    }
                    o1.createTime!! > o2.createTime!! -> {
                        -1
                    }
                    else -> {
                        1
                    }
                }
            })
        }
        //再次对文件夹排序
        Collections.sort(splitFolder, kotlin.Comparator { o1, o2 ->
            return@Comparator when {
                o1.level == o2.level -> {
                    0
                }
                o1.level > o2.level -> {
                    1
                }
                else -> {
                    -1
                }
            }
        })
        return splitFolder
    }


    private fun getMediaImageListNoSha1(): ArrayList<MediaInfo> {
        val query = BaseApp.app.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            mediaIamgeArrayof,
            null,
            null,
            //添加desc 后不能及时扫描最新的 照片
//            MediaStore.Images.Media.DATE_ADDED + " desc"
            MediaStore.Images.Media.DATE_ADDED
        )
        return getImageListByQuery(query,false)
    }

    private fun getImageListByQuery(query: Cursor?, isAddSha1: Boolean): ArrayList<MediaInfo> {
        var result = ArrayList<MediaInfo>()
        query?.let {
            try {
                query.moveToFirst()
                val filePathIndex = query.getColumnIndex(MediaStore.Images.Media.DATA)
                val fileNameIndex1 = query.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
                val createTimeIndex2 = query.getColumnIndex(MediaStore.Images.Media.DATE_ADDED)
                val idIndex3 = query.getColumnIndex(MediaStore.Images.Media._ID)
                val fileSizeIndex4 = query.getColumnIndex(MediaStore.Images.Media.SIZE)
                val latitudeIndex5 = query.getColumnIndex(MediaStore.Images.Media.LATITUDE)
                val longitudeIndex6 = query.getColumnIndex(MediaStore.Images.Media.LONGITUDE)
                val widthIndex7 = query.getColumnIndex(MediaStore.Images.Media.WIDTH)
                val heightIndex8 = query.getColumnIndex(MediaStore.Images.Media.HEIGHT)
                val mimeTypeIndex = query.getColumnIndex(MediaStore.Images.Media.MIME_TYPE)

                while (query.moveToNext()) {
                    // 获取图片的路径

                    val filePath: String = query.getString(filePathIndex)
                    //获取图片名称
                    val fileName: String = query.getString(fileNameIndex1)
                    //获取图片时间
                    val createTime: Long = query.getLong(createTimeIndex2)
                    val id: Long = query.getLong(idIndex3)
                    val fileSize: Long = query.getLong(fileSizeIndex4)
                    val latitude: Float = query.getFloat(latitudeIndex5)
                    val longitude: Float = query.getFloat(longitudeIndex6)
                    val width: Int = query.getInt(widthIndex7)
                    val height: Int = query.getInt(heightIndex8)
                    //获取图片类型
                    val mimeType: String = query.getString(
                        mimeTypeIndex
                    )
                    if (filePath.isNullOrEmpty()) {
                        continue
                    }
                    if (isSafePath(filePath)) { //过滤未下载完成的文件
    //                            LogUtils.d("width:", width, " height:", height)
                        val element = MediaInfo(
                            id,
                            fileName,
                            filePath,
                            fileSize,
                            -1,
                            mimeType,
                            false,
                            createTime,
                            null,
                            latitude,
                            longitude,
                            width,
                            height
                        )
                        if (isAddSha1) {
                            element.sha1 = filePath.let {
                                SHAMD5Utils.getSHA1(filePath)
                            }
                        }
                        result.add(element)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                query.close()
            }
            LogUtils.d("result.size:", result.size)
        }
        return result
    }

    private fun getMediaVideoListNoSha1(): ArrayList<MediaInfo> {
        val query = BaseApp.app.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            mediaVideoArrayof,
//            where,
            null,
//            whereArgs,
            null,
            //添加desc 后不能及时扫描最新的 照片
//            MediaStore.Video.Media.DATE_ADDED + " desc"
            MediaStore.Video.Media.DATE_ADDED
        )
        var result = getVideoListByQuery(query,false)
        return result

    }

    private fun getVideoListByQuery(query: Cursor?,isAddSha1:Boolean): ArrayList<MediaInfo> {
        var result = ArrayList<MediaInfo>()
        query?.let {
            try {
                query.moveToFirst()
                val idcolumnIndex = query.getColumnIndex(MediaStore.Video.Media._ID)
                val filePathcolumnIndex1 = query.getColumnIndex(MediaStore.Video.Media.DATA)
                val widthcolumnIndex2 = query.getColumnIndex(MediaStore.Video.Media.WIDTH)
                val heightcolumnIndex3 = query.getColumnIndex(MediaStore.Video.Media.HEIGHT)
                val fileNamecolumnIndex4 = query.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME)
                val createTimecolumnIndex5 = query.getColumnIndex(MediaStore.Video.Media.DATE_ADDED)
                val fileSizecolumnIndex6 = query.getColumnIndex(MediaStore.Video.Media.SIZE)
                val durationcolumnIndex7 = query.getColumnIndex(MediaStore.Video.Media.DURATION)
                val mimeTypecolumnIndex8 = query.getColumnIndex(MediaStore.Images.Media.MIME_TYPE)
                val latitudecolumnIndex9 = query.getColumnIndex(MediaStore.Video.Media.LATITUDE)
                val longitudecolumnIndex10 = query.getColumnIndex(MediaStore.Video.Media.LONGITUDE)

                while (query.moveToNext()) {
                    val id: Long = query.getLong(idcolumnIndex)
                    // 获取图片的路径
                    val filePath: String = query.getString(filePathcolumnIndex1)
                    val width: Int = query.getInt(widthcolumnIndex2)
                    val height: Int = query.getInt(heightcolumnIndex3)
                    //获取图片名称
                    val fileName: String = query.getString(fileNamecolumnIndex4)
                    //获取图片时间
                    val createTime: Long = query.getLong(createTimecolumnIndex5)
                    val fileSize: Long = query.getLong(fileSizecolumnIndex6)
                    val duration: Long = query.getLong(durationcolumnIndex7)
                    //获取图片类型
                    val mimeType: String = query.getString(mimeTypecolumnIndex8)
                    val latitude: Float = query.getFloat(latitudecolumnIndex9)
                    val longitude: Float = query.getFloat(longitudecolumnIndex10)
                    if (fileSize < 1024 || filePath.isNullOrEmpty()) {
                        continue
                    }
                    if (isSafePath(filePath)) { //过滤未下载完成的文件
                        LogUtils.d("width:", width, " height:", height)
                        val element = MediaInfo(
                            id,
                            fileName,
                            filePath,
                            fileSize,
                            duration,
                            mimeType,
                            true,
                            createTime,
                            null,
                            latitude,
                            longitude,
                            width,
                            height
                        )
                        var createTimess = FormatStrUtils.getFormatTimeStr(duration)
                        element.timestr = createTimess
                        if (isAddSha1) {
                            element.sha1 = filePath.let {
                                SHAMD5Utils.getSHA1(filePath)
                            }
                        }
                        result.add(element)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                query.close()
            }
            LogUtils.d("result.size:", result.size)
        }
        return result
    }

    private fun isSafePath(filePath: String):Boolean{
        return true;
//        return "downloading" != getExtensionName(filePath)
    }


    fun getExtensionName(dotname: String): String {
        return dotname?.substring(dotname.lastIndexOf("."))
    }

    /**
     * 把图片按文件夹拆分，第一个文件夹保存所有的图片
     *
     * @param images
     *
     * @return
     */
    open fun splitFolder(
        title: String,
        images: ArrayList<MediaInfo>
    ): ArrayList<Folder> {
        val folders = ArrayList<Folder>()
        val foldersMap = HashMap<String,Folder>()
        folders.add(Folder(title, images))
        if (!images.isEmpty()) {
//            val size = images.size
//            for (i in 0 until size) {
//            val images1 =images[i]
            for (images1 in images) {
                val path: String? = images1.filePath
                path?.let {
                    val name: String = getFolderName(path)
                    if (!TextUtils.isEmpty(name)) {
                        var newFolder = foldersMap.get(name);
                        if (newFolder == null) {
                            //如果文件夹不存在，重新生成新的文件夹
                            newFolder = Folder(name)
                            folders.add(newFolder)
                            foldersMap.put(name, newFolder)
                        }
                        newFolder.addImage(images1)
                    }
                }
            }
        }
        return folders
    }

    /**
     * 根据图片路径，获取图片文件夹名称
     *
     * @param path
     *
     * @return
     */
    private fun getFolderName(path: String): String {
        if (!TextUtils.isEmpty(path)) {
            val strings = path.split(File.separator).toTypedArray()
            if (strings.size >= 2) {
                return strings[strings.size - 2]
            }
        }
        return ""
    }


    companion object {
        private var mediaInfo: MediaInfoScanHelper? = null
        fun newInstance() = mediaInfo ?: synchronized(this) {
            mediaInfo ?: MediaInfoScanHelper().also { mediaInfo = it }
        }
    }

}