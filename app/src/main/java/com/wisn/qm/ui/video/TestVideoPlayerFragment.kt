package com.wisn.qm.ui.video

import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import com.blankj.utilcode.util.LogUtils
import com.danikula.videocache.HttpProxyCacheServer
import com.qmuiteam.qmui.arch.QMUIFragment
import com.we.player.controller.component.TitleControlView
import com.we.player.controller.controller.StandardController
import com.we.player.player.ScreenConfig
import com.we.playerexo.ExoPlayerFactory
import com.we.player.render.impl.TextureRenderViewFactory
import com.we.player.view.VideoView
import com.wisn.qm.R
import com.wisn.qm.mode.cache.ProxyVideoCacheManager
import com.wisn.qm.ui.testUrl

/**
 *
 * @Description:
 * @Author: Wisn
 * @CreateDate: 2020/11/13 下午3:15
 */
class TestVideoPlayerFragment : QMUIFragment(), View.OnClickListener {
    val TAG: String = "TestVideoPlayerFragment"
    val videoList = testUrl.getVideoListMY();
    var videoview: VideoView? = null
    var speedsBt: Button? = null
    override fun onCreateView(): View {
        return LayoutInflater.from(activity).inflate(R.layout.test_fragment_videoplayer, null)
    }

    override fun onViewCreated(rootView: View) {
        super.onViewCreated(rootView)
        videoview = rootView.findViewById<VideoView>(R.id.videoview)
        videoview?.setUrl(videoList.get(0).videoUrl)
        videoview?.renderViewFactory = TextureRenderViewFactory()
//        videoview?.mIRenderView = SurfaceRenderView(requireContext())
        videoview?.mediaPlayer = ExoPlayerFactory()
//        videoview?.mediaPlayer = AndroidMediaPlayerFactory()
        var standardController = StandardController(requireContext());
//        standardController.addIViewItemControllerOne(PlayControlView(requireContext()))
        videoview?.iViewController = standardController
        videoview?.setLooping(true)
        videoview?.isMute = true

        rootView.findViewById<View>(R.id.start).setOnClickListener(this)
        rootView.findViewById<View>(R.id.scaletype).setOnClickListener(this)
        rootView.findViewById<View>(R.id.mirrorRotion).setOnClickListener(this)
        rootView.findViewById<View>(R.id.Rotation).setOnClickListener(this)
        rootView.findViewById<View>(R.id.startNew).setOnClickListener(this)
        rootView.findViewById<View>(R.id.nextV).setOnClickListener(this)
        speedsBt = rootView.findViewById(R.id.Speeds)
        speedsBt?.setOnClickListener(this)
        standardController.titleControlView?.backListener = object : TitleControlView.BackListener {
            override fun back() {
                super.back()
                popBackStack()
            }
        }

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        LogUtils.d(TAG, " onOrientationChanged  onConfigurationChanged!!!! $newConfig")

    }

    override fun translucentFull(): Boolean {
        return true
    }

    var isMirror: Boolean = false
    var degree: Int = 0
    var speeds: Float = 1f
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.start -> {
                videoview?.start()
            }
            R.id.scaletype -> {
                when (videoview?.mCurrentScreenScaleType) {
                    ScreenConfig.SCREEN_SCALE_DEFAULT -> {
                        videoview?.mCurrentScreenScaleType = ScreenConfig.SCREEN_SCALE_16_9
                    }
                    ScreenConfig.SCREEN_SCALE_16_9 -> {
                        videoview?.mCurrentScreenScaleType = ScreenConfig.SCREEN_SCALE_4_3
                    }
                    ScreenConfig.SCREEN_SCALE_4_3 -> {
                        videoview?.mCurrentScreenScaleType = ScreenConfig.SCREEN_SCALE_MATCH_PARENT
                    }
                    ScreenConfig.SCREEN_SCALE_MATCH_PARENT -> {
                        videoview?.mCurrentScreenScaleType = ScreenConfig.SCREEN_SCALE_ORIGINAL
                    }
                    ScreenConfig.SCREEN_SCALE_ORIGINAL -> {
                        videoview?.mCurrentScreenScaleType = ScreenConfig.SCREEN_SCALE_CENTER_CROP
                    }
                    ScreenConfig.SCREEN_SCALE_CENTER_CROP -> {
                        videoview?.mCurrentScreenScaleType = ScreenConfig.SCREEN_SCALE_DEFAULT
                    }
                }
            }
            R.id.mirrorRotion -> {
                isMirror = !isMirror
                videoview?.setMirrorRotation(isMirror)
            }
            R.id.Rotation -> {
                degree + 90
                videoview?.setVideoRotation(degree)

            }
            R.id.Speeds -> {
                speeds = speeds + 0.5f
                if (speeds > 10) {
                    speeds = 1f
                }
                videoview?.setSpeed(speeds)
                speedsBt?.setText("speed$speeds")
            }
            R.id.nextV -> {
                if (index >= (videoList.size - 1)) {
                    index = 0
                }
                index++
                val get = videoList.get(index)
//                videoview?.stop()
                val cacheServer: HttpProxyCacheServer = ProxyVideoCacheManager.getProxy(requireContext())
                val proxyUrl = cacheServer.getProxyUrl(get.videoUrl)
                videoview?.release()
                videoview?.setUrl(proxyUrl)
                videoview?.start()
            }

            R.id.startNew -> {
                if (index >= (videoList.size - 1)) {
                    index = 0
                }
                index++
                val get = videoList.get(index)
//
                val videoPlayerFragment = VideoPlayerFragment(get.videoUrl, get.thumb, get.title, false)
                startFragment(videoPlayerFragment)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        videoview?.resume()
    }

    override fun onPause() {
        super.onPause()
        videoview?.pause()

    }

    override fun onStop() {
        super.onStop()
        videoview?.stop()
    }

    override fun onDestroyView() {
        super.onDestroyView()

    }

    override fun onDestroy() {
        super.onDestroy()
        videoview?.release()
    }


    var index: Int = 0;

    override fun onBackPressed() {
        if (videoview?.onBackPressed() == true) {
            return
        }
        super.onBackPressed()
    }
}