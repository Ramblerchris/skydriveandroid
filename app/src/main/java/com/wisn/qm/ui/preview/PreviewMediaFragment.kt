package com.wisn.qm.ui.preview

import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.Px
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.ScrollState
import com.bumptech.glide.Glide
import com.bumptech.glide.request.transition.Transition
import com.library.base.BaseApp
import com.library.base.BaseFragment
import com.library.base.base.NoViewModel
import com.library.base.base.ViewModelFactory
import com.library.base.config.GlobalConfig
import com.library.base.glide.FileTarget
import com.library.base.glide.progress.ProgressManager
import com.library.base.utils.*
import com.qmuiteam.qmui.kotlin.onClick
import com.qmuiteam.qmui.skin.QMUISkinManager
import com.qmuiteam.qmui.util.QMUIStatusBarHelper
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet
import com.we.player.render.impl.TextureRenderViewFactory
import com.we.player.view.VideoView
import com.we.playerexo.ExoPlayerFactory
import com.wisn.qm.R
import com.wisn.qm.mode.beans.FileType
import com.wisn.qm.mode.beans.PreviewImage
import com.wisn.qm.mode.cache.PreloadManager
import com.wisn.qm.mode.db.beans.MediaInfo
import com.wisn.qm.ui.album.newalbum.NewAlbumFragment
import com.wisn.qm.ui.home.HomeViewModel
import com.wisn.qm.ui.preview.listener.LoadOriginCallBack
import com.wisn.qm.ui.preview.view.NetListVideoController
import com.wisn.qm.ui.preview.viewholder.PreviewVideoViewHolder
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File


class PreviewMediaFragment(var data: MutableList<out PreviewImage>, var initSelectPosition: Int) :
    BaseFragment<NoViewModel>(), PreviewMediaCallback {
    var recyclerView: RecyclerView? = null
    var playPosition: Int? = null
    var selectPosition: Int = 0
    var size: Int = data.size
    var mPreloadManager: PreloadManager? = null
    val previewMediaAdapter by lazy {
        PreviewMediaAdapter(data, this@PreviewMediaFragment)
    }
    val videoView by lazy {
        var videoview = VideoView(BaseApp.app)
        videoview.renderViewFactory = TextureRenderViewFactory()
        videoview.mediaPlayer = ExoPlayerFactory()
//        videoview.mediaPlayer = AndroidMediaPlayerFactory()
        videoview.iViewController = NetListVideoController(requireContext())
        videoview.setLooping(true)
        videoview
    }

    override fun layoutId(): Int {
        return R.layout.fragment_preview
    }

    var fl_online: FrameLayout? = null;
    var fl_local: LinearLayout? = null;
    var top_bg: ConstraintLayout? = null;
    var vp_content: ViewPager2? = null;
    var img_download: ImageView? = null;
    var iv_back: ImageView? = null;
    var btn_show_origin: Button? = null;
    var tv_addto: TextView? = null;
    var tv_upload: TextView? = null;
    var indicator_tv: TextView? = null;

    override fun initView(views: View) {
        super.initView(views)
        iv_back = views.findViewById<ImageView>(R.id.iv_back)
        fl_online = views.findViewById<FrameLayout>(R.id.fl_online)
        fl_local = views.findViewById<LinearLayout>(R.id.fl_local)
        top_bg = views.findViewById<ConstraintLayout>(R.id.top_bg)
        vp_content = views.findViewById<ViewPager2>(R.id.vp_content)
        img_download = views.findViewById<ImageView>(R.id.img_download)
        btn_show_origin = views.findViewById<Button>(R.id.btn_show_origin)
        tv_addto = views.findViewById<Button>(R.id.tv_addto)
        tv_upload = views.findViewById<Button>(R.id.tv_upload)
        indicator_tv = views.findViewById<Button>(R.id.indicator_tv)
        mPreloadManager = PreloadManager.getInstance(requireContext())
        QMUIStatusBarHelper.setStatusBarDarkMode(activity)
        var mHomeViewModel =
            ViewModelProvider(requireActivity(), ViewModelFactory()).get(HomeViewModel::class.java)
        indicator_tv?.text="1/${size}"
        indicator_tv?.setTypeface(Typeface.MONOSPACE)
        btn_show_origin?.setTypeface(Typeface.MONOSPACE)
        vp_content?.overScrollMode = View.OVER_SCROLL_NEVER
        vp_content?.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            override fun onPageScrolled(
                position: Int, positionOffset: Float,
                @Px positionOffsetPixels: Int
            ) {
            }


            override fun onPageSelected(position: Int) {
                indicator_tv?.text="${position+1}/${size}"
                selectPosition = position
                val get = data.get(position)
                dealBottom(position,get)
                if (playPosition == position) {
                    //如果是当前的 返回
                    return
                }

                if (get.itemType != FileType.VideoViewItem) {
                    videoView.pause()
//                    recycleVideoView()
                    return
                }
                startPlay(position)
            }


            override fun onPageScrollStateChanged(@ScrollState state: Int) {
//                ViewPager2.SCROLL_STATE_IDLE, ViewPager2.SCROLL_STATE_DRAGGING, ViewPager2.SCROLL_STATE_SETTLING
                when (state) {
                    ViewPager2.SCROLL_STATE_IDLE -> {
                        //空闲状态
                       /* val get = data.get(SelectPosition)
                        dealBottom(SelectPosition,get)*/
                        if (selectPosition == playPosition) {
                            if (!videoView.isPlaying()) {
                                videoView.resume()
                            }
                        }
                    }
                    ViewPager2.SCROLL_STATE_DRAGGING -> {
                        //滑动状态
//                        videoView.pause()
                        /*fl_online?.visibility = View.GONE
                        fl_local?.visibility = View.GONE*/
                    }
                    ViewPager2.SCROLL_STATE_SETTLING -> {
                        //滑动后自然沉降的状态
//                        videoView.pause()

                    }
                }
            }

        })

        tv_addto?.onClick {
            val values = mHomeViewModel.getUserDirlist().value
            var addItem = View.inflate(context, R.layout.item_album_new_album, null)
            values?.let {
                val builder = QMUIBottomSheet.BottomListSheetBuilder(activity)
                builder.setGravityCenter(true)
                    .setSkinManager(QMUISkinManager.defaultInstance(context))
                    .setTitle("添加到")
                    .setAddCancelBtn(true)
                    .setAllowDrag(true)
                    .setNeedRightMark(true)
                    .setOnSheetItemClickListener { dialog, itemView, position, tag ->
                        dialog.dismiss()
                        val get = data.get(selectPosition)
                        if (get is MediaInfo) {
                            mHomeViewModel.saveMedianInfo(position, get, false)
                        }
                        MToastUtils.show("已经添加到上传任务")
                    }
                for (dirlist in values) {
                    builder.addItem(dirlist.filename)
                }

                builder.addContentFooterView(addItem)
                val build = builder.build()
                build.show()
                addItem.onClick {
                    build.dismiss()
                    startFragment(NewAlbumFragment())
                }
            }
        }
        tv_upload?.onClick {
            val get = data.get(vp_content?.currentItem!!)
            if (get is MediaInfo) {
                mHomeViewModel.saveMedianInfo(0, get, false)
                MToastUtils.show("已经添加到上传任务")
            }

        }
        img_download?.onClick {
            /*try {
                val get = data.get(vp_content?.currentItem!!)
                DownloadFileUtils.downloadPicture(requireContext(),get.resourcePath,true)
            } catch (e: Exception) {
            }*/
            val get = data.get(vp_content?.currentItem!!)
            downloadOrigin(selectPosition,get,true)

        }
        iv_back?.onClick {
           popBackStack()
        }
        indicator_tv?.onClick {
            val get = data.get(vp_content?.currentItem!!)
            ClipboardUtils.copy(requireContext(),get.resourceThumbNailPath);
        }


        recyclerView = vp_content?.getChildAt(0) as RecyclerView?

        vp_content?.adapter = previewMediaAdapter

        vp_content?.setCurrentItem(initSelectPosition, false)

        vp_content?.post(Runnable {
            startPlay(initSelectPosition)
        })
    }


    override fun onFetchTransitionConfig(): TransitionConfig {
        return SCALE_TRANSITION_CONFIG
    }


    fun startPlay(position: Int) {
        recyclerView?.children?.forEach {
            if (it.tag is PreviewVideoViewHolder) {
                var viewholder = it.tag as PreviewVideoViewHolder
                if (position == viewholder.pos) {
                    playItemView(position, viewholder)
                    return@forEach
                }
            }
        }
    }

    private fun playItemView(position: Int, viewholder: PreviewVideoViewHolder) {
        videoView.release()
        recycleVideoView()
        val get = data.get(position)
//        videoView.setUrl(get.resourcePath!!)
        if (get.isLocal) {
            videoView.setUrl(get.resourcePath!!)
        } else {
            val playUrl = mPreloadManager!!.getPlayUrl(get.resourcePath)
            videoView.setUrl(playUrl)
        }
        videoView.iViewController?.addIViewItemControllerOne(viewholder.preview, true)
        viewholder.content.addView(videoView, 0)
        videoView.start()
        playPosition = position
    }


    fun recycleVideoView() {
        videoView.parent?.let {
            it as ViewGroup
            it.removeView(videoView)
        }
    }

    override fun callBackLocal(view: View) {
        if (top_bg?.visibility == View.GONE) {
            top_bg?.visibility = View.VISIBLE
            val get = data.get(selectPosition)
            if(get.itemType == FileType.ImageViewItem){
                if (get.isLocal) {
                    fl_online?.visibility = View.GONE
                    fl_local?.visibility = View.VISIBLE
                }else{
                   /* val originUrl = CacheUrl.getOriginUrl(get.resourcePath!!)
                    if (originUrl.isNullOrEmpty()) {
                        fl_online?.visibility = View.VISIBLE
                        fl_local?.visibility = View.GONE
                    }*/
                    fl_online?.visibility = View.VISIBLE
                    fl_local?.visibility = View.GONE
                }
            }
        } else {
            top_bg?.visibility = View.GONE
            fl_online?.visibility = View.GONE
            fl_local?.visibility = View.GONE
        }
    }

    override fun callBackOnLine(position: Int, mediainfo: PreviewImage,isShowLoadOrigin :Boolean,
                                loadOriginCallBack: LoadOriginCallBack?) {
        Log.d("callBackOnLine","${position} ${mediainfo.resourcePath}")
    }

    private fun dealBottom(position: Int, previewImageBean: PreviewImage) {
        if (previewImageBean.itemType == FileType.VideoViewItem) {
            fl_online?.visibility = View.GONE
            fl_local?.visibility = View.GONE
        } else {
            if (previewImageBean.isLocal) {
                fl_online?.visibility = View.GONE
                fl_local?.visibility = View.VISIBLE
            } else {
                fl_online?.visibility = View.VISIBLE
                fl_local?.visibility = View.GONE
                //dodo 是否显示加载原图
                val glideCacheFile = GlideUtils.getGlideCacheFile(requireContext(),previewImageBean.resourcePath!!)

                if (glideCacheFile == null || !glideCacheFile.exists()) {
                    if(GlobalConfig.previewImageOrigin){
                        downloadOrigin(position,previewImageBean,false);
                    }else{
                        btn_show_origin?.visibility = View.VISIBLE
                        val resourceSizeStr = previewImageBean.getResourceSizeStr()
                        if (TextUtils.isEmpty(resourceSizeStr)) {
                            btn_show_origin?.text = "加载原图"
                        } else {
                            btn_show_origin?.text = "加载原图(${resourceSizeStr})"
                        }
                        btn_show_origin?.setOnClickListener {
                            downloadOrigin(position,previewImageBean,false);
                        }
                    }

                } else {
                    btn_show_origin?.visibility = View.GONE
                }
            }
        }
    }

    fun downloadOrigin(position: Int,mediainfo: PreviewImage,isSave:Boolean){
        btn_show_origin?.text="加载中"
        Log.d("callBackOnLine","${position} loadOrigin"+mediainfo.resourcePath!!)
        Glide.with(requireContext()).downloadOnly().load(mediainfo.resourcePath!!)
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
                    CacheUrl.addOriginUrl(mediainfo.resourcePath!!,resource.absolutePath)
                    previewMediaAdapter.notifyItemChanged(position)
                    btn_show_origin?.visibility=View.GONE
                    if(isSave){
                        MToastUtils.show("图片已下载到手机")
                        GlobalScope.launch {
                            DownloadImageFileUtils.saveFileAndUpdateAlbum(resource,requireContext())
                            CacheUrl.addDownloadUrl(mediainfo.resourcePath!!,resource.absolutePath)
                        }
                    }
//                    fl_online?.visibility = View.GONE
                }
            })

        ProgressManager.addListener(mediainfo.resourcePath!!
        ) { url, isComplete, percentage, bytesRead, totalBytes ->
            Log.d("addListener",
                "isComplete:${isComplete} percentage:${percentage} ")
            activity?.runOnUiThread {

                if(isComplete){

                }else{
                    btn_show_origin?.text="${percentage}%"
                }
            }
        }
    }
    override fun playViewPosition(previewVideoViewHolder: PreviewVideoViewHolder, position: Int) {
        playItemView(position, previewVideoViewHolder);
    }

    override fun onDestroy() {
        super.onDestroy()
        videoView.release()
        QMUIStatusBarHelper.setStatusBarLightMode(activity)
    }


    override fun onResume() {
        super.onResume()
        videoView.resume()
    }

    override fun onPause() {
        super.onPause()
        videoView.pause()

    }

    override fun onStop() {
        super.onStop()
        videoView.stop()
    }


}