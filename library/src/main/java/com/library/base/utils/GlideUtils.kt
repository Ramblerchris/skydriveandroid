package com.library.base.utils

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.library.R
import com.library.base.BaseApp
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


}