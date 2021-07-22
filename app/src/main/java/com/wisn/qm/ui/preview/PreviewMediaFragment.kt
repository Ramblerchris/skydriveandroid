package com.wisn.qm.ui.preview

import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.Px
import androidx.core.view.children
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.ScrollState
import com.library.base.BaseApp
import com.library.base.BaseFragment
import com.library.base.base.NoViewModel
import com.library.base.base.ViewModelFactory
import com.library.base.utils.MToastUtils
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
import com.wisn.qm.ui.preview.view.NetListVideoController
import com.wisn.qm.ui.preview.viewholder.PreviewVideoViewHolder
import kotlinx.android.synthetic.main.fragment_preview.*


class PreviewMediaFragment(var data: MutableList<out PreviewImage>, var position: Int) :
    BaseFragment<NoViewModel>(), PreviewMediaCallback {
    var recyclerView: RecyclerView? = null
    var playPosition: Int? = null
    var SelectPosition: Int? = null
    var mPreloadManager: PreloadManager? = null

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
    var top_bg: LinearLayout? = null;
    var vp_content: ViewPager2? = null;
    var img_download: ImageView? = null;
    var btn_show_origin: Button? = null;
    var tv_addto: TextView? = null;
    var tv_upload: TextView? = null;

    override fun initView(views: View) {
        super.initView(views)
        fl_online = views.findViewById<FrameLayout>(R.id.fl_online)
        fl_local = views.findViewById<LinearLayout>(R.id.fl_local)
        top_bg = views.findViewById<LinearLayout>(R.id.top_bg)
        vp_content = views.findViewById<ViewPager2>(R.id.vp_content)
        img_download = views.findViewById<ImageView>(R.id.img_download)
        btn_show_origin = views.findViewById<Button>(R.id.btn_show_origin)
        tv_addto = views.findViewById<Button>(R.id.tv_addto)
        tv_upload = views.findViewById<Button>(R.id.tv_upload)
        mPreloadManager = PreloadManager.getInstance(requireContext())
        QMUIStatusBarHelper.setStatusBarDarkMode(activity)
        var mHomeViewModel =
            ViewModelProvider(requireActivity(), ViewModelFactory()).get(HomeViewModel::class.java)


        vp_content?.overScrollMode = View.OVER_SCROLL_NEVER
        vp_content?.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            override fun onPageScrolled(
                position: Int, positionOffset: Float,
                @Px positionOffsetPixels: Int
            ) {
            }


            override fun onPageSelected(position: Int) {
                SelectPosition = position
                if (playPosition == position) {
                    //如果是当前的 返回
                    return
                }
                val get = data.get(position)
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
                        if (SelectPosition == playPosition) {
                            if (!videoView.isPlaying()) {
                                videoView.resume()
                            }
                        }
                    }
                    ViewPager2.SCROLL_STATE_DRAGGING -> {
                        //滑动状态
//                        videoView.pause()
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
                        mHomeViewModel.saveMedianInfo(position, false)
                        MToastUtils.show("已经添加到上传任务")
                    }
                for (dirlist in values) {
                    builder.addItem(dirlist.filename)
                }

                builder.addContentFooterView(addItem)
                val build = builder.build();
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
        iv_back?.onClick {
            popBackStack()
        }

        recyclerView = vp_content?.getChildAt(0) as RecyclerView?

        vp_content?.adapter = PreviewMediaAdapter(data, this@PreviewMediaFragment)

        vp_content?.setCurrentItem(position, false)

        vp_content?.post(Runnable {

            startPlay(position)
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
        } else {
            top_bg?.visibility = View.GONE
        }
    }

    override fun callBackOnLine(position: Int, mediainfo: PreviewImage,isShowLoadOrigin :Boolean) {
        fl_online?.visibility=View.VISIBLE
        fl_local?.visibility=View.GONE
        if(isShowLoadOrigin){
            btn_show_origin?.visibility=View.VISIBLE
        }else{
            btn_show_origin?.visibility=View.GONE
        }
    }

    override fun playViewPosition(previewVideoViewHolder: PreviewVideoViewHolder, position: Int) {
        playItemView(position, previewVideoViewHolder);
    }

    override fun onDestroy() {
        super.onDestroy()
        videoView.release()
        QMUIStatusBarHelper.setStatusBarLightMode(activity)
//        UploadTaskUitls.exeRequest(Utils.getApp(), UploadTaskUitls.buildUploadRequest())

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