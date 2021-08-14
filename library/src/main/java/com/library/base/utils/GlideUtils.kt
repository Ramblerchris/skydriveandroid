package com.library.base.utils

import android.content.Context
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.disklrucache.DiskLruCache
import com.bumptech.glide.load.engine.cache.DiskCache
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.EmptySignature
import com.library.R
import com.library.base.BaseApp
import com.library.base.glide.cache.DataCacheKey
import com.library.base.glide.cache.SafeKeyGenerator
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

/**
 *
 * @Description:
 * @Author: Wisn
 * @CreateDate: 2020/11/25 下午3:35
 */
object GlideUtils {
 //   placeholder //  正在请求图片的时候展示的图片
//   error    //  如果请求失败的时候展示的图片 （如果没有设置，还是展示placeholder的占位符）
//   fallback //  如果请求的url/model为 null 的时候展示的图片 （如果没有设置，还是展示placeholder的占位符）
    val option = RequestOptions().error(R.drawable.ic_no_media).placeholder(R.drawable.ic_no_media).fallback(R.drawable.ic_no_media)

    fun loadFile(path: String, imageView: ImageView) {
        Glide.with(BaseApp.app)
                .load(File(path))
                .apply(option)
                .into(imageView)
    }
    fun loadFile(path: String, imageView: ImageView,error: Int? = R.drawable.ic_no_media, placeholder: Int? = R.drawable.ic_no_media, fallback: Int? = R.drawable.ic_no_media) {
        val placeholder = RequestOptions().error(error!!).placeholder(placeholder!!).fallback(fallback!!)
        Glide.with(BaseApp.app)
                .load(File(path))
                .apply(placeholder)
                .into(imageView)
    }

    fun loadUrl(url: String, imageView: ImageView, error: Int? = R.drawable.ic_no_media, placeholder: Int? = R.drawable.ic_no_media, fallback: Int? = R.drawable.ic_no_media) {
        val placeholder = RequestOptions().error(error!!).placeholder(placeholder!!).fallback(fallback!!)
        Glide.with(BaseApp.app)
                .load(url)
                .apply(placeholder)
                .into(imageView)
    }

    fun loadUrlNoOP(url: String, imageView: ImageView) {
        Glide.with(BaseApp.app)
                .load(url)
                .into(imageView)
    }


    /**
     * 获取是否有某张原图的缓存
     * 缓存模式必须是：DiskCacheStrategy.SOURCE 才能获取到缓存文件
     */
    fun getGlideCacheFile(context: Context, url: String?): File? {
        try {
            val dataCacheKey = DataCacheKey(GlideUrl(url), EmptySignature.obtain())
//            val safeKeyGenerator = SafeKeyGenerator()
            val safeKey = SafeKeyGenerator.getSafeKey(dataCacheKey)
            val file = File(context.cacheDir, DiskCache.Factory.DEFAULT_DISK_CACHE_DIR)
            val diskLruCache =
                DiskLruCache.open(file, 1, 1, DiskCache.Factory.DEFAULT_DISK_CACHE_SIZE.toLong())
            val value = diskLruCache[safeKey]
            if (value != null) {
                return value.getFile(0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun clearMemory(activity: AppCompatActivity) {
        Glide.get(activity.applicationContext).clearMemory()
    }

    fun cleanDiskCache(context: Context) {
        GlobalScope.launch {
            Glide.get(context.applicationContext).clearDiskCache()
        }
    }


}