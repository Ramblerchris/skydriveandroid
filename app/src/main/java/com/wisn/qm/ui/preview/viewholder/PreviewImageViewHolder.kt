package com.wisn.qm.ui.preview.viewholder

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.transition.Transition
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.github.chrisbanes.photoview.PhotoView
import com.library.base.config.GlobalConfig
import com.library.base.glide.FileTarget
import com.library.base.glide.progress.ProgressManager
import com.library.base.utils.GlideUtils
import com.library.base.utils.ImageUtils
import com.qmuiteam.qmui.kotlin.onClick
import com.qmuiteam.qmui.widget.QMUIProgressBar
import com.wisn.qm.BuildConfig
import com.wisn.qm.R
import com.wisn.qm.mode.beans.PreviewImage
import com.wisn.qm.ui.preview.PreviewMediaCallback
import com.wisn.qm.ui.preview.listener.SimpleOnImageEventListener
import java.io.File

class PreviewImageViewHolder(var context: Context, view: View, var previewCallback: PreviewMediaCallback) : BasePreviewHolder(view){
    var iv_image: SubsamplingScaleImageView = view.findViewById(R.id.iv_image)
    var gif_view: PhotoView = view.findViewById(R.id.gif_view)
    var test_info: TextView = view.findViewById(R.id.test_info)
//    var progress_view: ProgressBar = view.findViewById(R.id.progress_view)
    var circleProgressBar: QMUIProgressBar = view.findViewById(R.id.circleProgressBar)
    var position: Int?=0
    var mediainfo: PreviewImage?=null;
    init {
        iv_image.onClick {
            previewCallback.callBackLocal(it)
        }
        gif_view.onClick {
            previewCallback.callBackLocal(it)
        }
        iv_image.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE)
        iv_image.setDoubleTapZoomStyle(SubsamplingScaleImageView.ZOOM_FOCUS_CENTER)
        iv_image.setOrientation(SubsamplingScaleImageView.ORIENTATION_USE_EXIF)
        iv_image.setDoubleTapZoomDuration(200)
        iv_image.setMinScale(1f)
        iv_image.setMaxScale(5f)
        iv_image.setDoubleTapZoomScale(3f)

        gif_view.setZoomTransitionDuration(200)
        gif_view.setMinimumScale(1f)
        gif_view.setMaximumScale(5f)
        gif_view.setScaleType(ImageView.ScaleType.FIT_CENTER)
        if(BuildConfig.DEBUG){
            test_info.visibility=View.VISIBLE;
        }
    }

    override fun loadImage(position: Int, mediainfo: PreviewImage) {
        this.position=position
        this.mediainfo=mediainfo
//        progress_view.visibility=View.GONE
        circleProgressBar.visibility=View.GONE
        if(mediainfo.isLocal){
//            progress_view.visibility=View.GONE
            //本地图片直接展示
            val gifImageWithMime =
                ImageUtils.isGifImageWithMime(mediainfo.resourcePath!!, mediainfo.resourcePath!!)
            if ( gifImageWithMime) {
                iv_image.visibility = View.GONE
                gif_view.visibility = View.VISIBLE
                GlideUtils.loadUrlNoOP(mediainfo.resourcePath!!, gif_view)
            } else {
                iv_image.visibility = View.VISIBLE
                gif_view.visibility = View.GONE
                iv_image.setImage(ImageSource.uri(mediainfo.resourcePath!!))
            }
            test_info.text="本地"+mediainfo.resourcePath!!
        }else{
            gif_view.visibility = View.VISIBLE
            val glideCacheFile = GlideUtils.getGlideCacheFile(context, mediainfo.resourcePath!!)
            if(glideCacheFile==null||!glideCacheFile.exists()){
                GlideUtils.loadUrlNoOP(mediainfo.resourceThumbNailPath!!, gif_view)
                iv_image.visibility = View.GONE
                test_info.text="缩略图"+mediainfo.resourceThumbNailPath
                if(GlobalConfig.previewImageOrigin){
                    loadOrigin()
                }
            }else{
                //直接加载原图
                val gifImageWithMime =
                    ImageUtils.isGifImageWithMime(glideCacheFile.absolutePath, glideCacheFile.absolutePath)
                if ( gifImageWithMime) {
                    iv_image.visibility = View.GONE
                    gif_view.visibility = View.VISIBLE
                    GlideUtils.loadUrlNoOP(glideCacheFile.absolutePath, gif_view)
                } else {
                    iv_image.visibility = View.VISIBLE
                    gif_view.visibility = View.GONE
                    iv_image.setImage(ImageSource.uri(glideCacheFile.absolutePath))
                }
                test_info.text="原图"+glideCacheFile.absolutePath
            }
        }
    }

    fun loadOriginProgress(url:String ,isComplete:Boolean, progress:Int){

    }

    fun loadOrigin() {
        Log.d("callBackOnLine","${position} loadOrigin"+mediainfo?.resourcePath!!)
        circleProgressBar.visibility=View.VISIBLE
        circleProgressBar.setProgress(0,false)
        ProgressManager.addListener(mediainfo?.resourcePath!!) { url, isComplete, percentage, bytesRead, totalBytes ->
            Log.d("addListener",
                "isComplete:${isComplete} percentage:${percentage} ")
            if(isComplete){
                circleProgressBar.visibility=View.GONE
            }else{
                circleProgressBar.setProgress(percentage,false)
            }
        }
        iv_image.setTag(R.id.glide_loadurl,mediainfo?.resourcePath!!)
        Glide.with(context).downloadOnly().load(mediainfo?.resourcePath!!)
            .into(object : FileTarget() {
                override fun onLoadStarted(placeholder: Drawable?) {
                    super.onLoadStarted(placeholder)
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    super.onLoadFailed(errorDrawable)
//                    progress_view.visibility=View.GONE
                    circleProgressBar.visibility=View.GONE
                }

                override fun onResourceReady(
                    resource: File,
                    transition: Transition<in File?>?
                ) {
                    super.onResourceReady(resource, transition)
                    circleProgressBar.visibility=View.GONE
                    Log.d("callBackOnLine","onResourceReady"+mediainfo?.resourcePath!!)
                    if(iv_image.getTag(R.id.glide_loadurl).equals(mediainfo?.resourcePath!!)){
                        val gifImageWithMime = ImageUtils.isGifImageWithMime(resource.absolutePath)
                        if(!gifImageWithMime){
                            iv_image.visibility = View.VISIBLE
                            Log.d("setOnImageEventListener","setOnImageEventListenerAAAAAA"+gifImageWithMime)
                            iv_image.setOnImageEventListener(object : SimpleOnImageEventListener() {
                                override fun onReady() {
                                    super.onReady()
                                    Log.d("setOnImageEventListener","setOnImageEventListener"+gifImageWithMime)
//                                    progress_view.visibility=View.GONE
                                    gif_view.visibility = View.GONE
                                }
                            })
                            iv_image.setImage(ImageSource.uri(resource.absolutePath))
                            test_info.text="原图"+resource.absolutePath
                        }else{
//                            progress_view.visibility=View.GONE
                            iv_image.visibility = View.GONE
                            gif_view.visibility = View.VISIBLE
                            GlideUtils.loadFile(resource.absolutePath, gif_view)
                            test_info.text="原图"+resource.absolutePath
                        }
                    }
                }
            })
    }

}