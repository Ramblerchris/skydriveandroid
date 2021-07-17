package com.wisn.qm.ui.preview.viewholder

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.transition.Transition
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.github.chrisbanes.photoview.PhotoView
import com.library.base.utils.FileTarget
import com.library.base.utils.GlideUtils
import com.library.base.utils.ImageUtils
import com.qmuiteam.qmui.kotlin.onClick
import com.wisn.qm.R
import com.wisn.qm.mode.beans.PreviewImage
import com.wisn.qm.ui.preview.PreviewMediaCallback
import java.io.File

class PreviewImageViewHolder(var context: Context, view: View, var previewCallback: PreviewMediaCallback) : BasePreviewHolder(view) {
    var iv_image: SubsamplingScaleImageView = view.findViewById(R.id.iv_image)
    var gif_view: PhotoView = view.findViewById(R.id.gif_view)

    init {
        iv_image.onClick {
            previewCallback.onContentClick(it)
        }
        gif_view.onClick {
            previewCallback.onContentClick(it)
        }
        iv_image.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE)
        iv_image.setDoubleTapZoomStyle(SubsamplingScaleImageView.ZOOM_FOCUS_CENTER)
        iv_image.setDoubleTapZoomDuration(200)
        iv_image.setMinScale(1f)
        iv_image.setMaxScale(5f)
        iv_image.setDoubleTapZoomScale(3f)

        gif_view.setZoomTransitionDuration(200)
        gif_view.setMinimumScale(1f)
        gif_view.setMaximumScale(5f)
        gif_view.setScaleType(ImageView.ScaleType.FIT_CENTER)
    }

    override fun loadImage(position: Int, mediainfo: PreviewImage) {
//        if (ImageUtils.isGifImageWithMime(mediainfo.filePath!!, mediainfo.filePath!!)) {
        if(mediainfo.isLocal){
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
        }else{
            iv_image.visibility = View.GONE
            gif_view.visibility = View.VISIBLE
            GlideUtils.loadUrlNoOP(mediainfo.resourcePath!!, gif_view)
            Glide.with(context).downloadOnly().load(mediainfo.resourcePath!!)
                .into(object : FileTarget() {
                    override fun onLoadStarted(placeholder: Drawable?) {
                        super.onLoadStarted(placeholder)
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        super.onLoadFailed(errorDrawable)
                    }

                    override fun onResourceReady(
                        resource: File,
                        transition: Transition<in File?>?
                    ) {
                        super.onResourceReady(resource, transition)
                        val gifImageWithMime = ImageUtils.isGifImageWithMime(resource.absolutePath)
                        if(!gifImageWithMime){
                            iv_image.visibility = View.VISIBLE
                            gif_view.visibility = View.GONE
                            iv_image.setImage(ImageSource.uri(resource.absolutePath))
                        }
                    }
                })
        }
    }

    override fun loadVideo(position: Int, mediainfo: PreviewImage) {

    }

}