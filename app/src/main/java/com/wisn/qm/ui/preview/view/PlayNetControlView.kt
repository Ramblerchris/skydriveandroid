package com.wisn.qm.ui.preview.view

import android.content.Context
import android.content.pm.ActivityInfo
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.widget.*
import com.blankj.utilcode.util.LogUtils
import com.we.player.controller.IViewController
import com.we.player.controller.IViewItemController
import com.we.player.player.PlayStatus
import com.we.player.utils.TimeStrUtils
import com.we.player.view.MediaPlayerController
import com.wisn.qm.R

/**
 *
 * @Description:
 * @Author: Wisn
 * @CreateDate: 2020/11/14 下午10:57
 */
class PlayNetControlView : FrameLayout, IViewItemController, View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    var TAG: String? = "PlayControlView"

    var mediaPlayerController: MediaPlayerController? = null
    var iViewController: IViewController? = null
    var bottom_container: LinearLayout? = null
    var curr_time: TextView? = null
    var seekBar: SeekBar? = null
    var total_time: TextView? = null
    var bottom_progress: ProgressBar? = null
    var isCanChage: Boolean = true

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : super(context, attributeSet, defStyleAttr) {
        LayoutInflater.from(getContext()).inflate(R.layout.item_controller_localplay, this, true)
        bottom_container = this.findViewById<LinearLayout>(R.id.bottom_container)

        curr_time = this.findViewById<TextView>(R.id.curr_time)
        seekBar = this.findViewById<SeekBar>(R.id.seekBar)
        total_time = this.findViewById<TextView>(R.id.total_time)
        bottom_progress = this.findViewById<ProgressBar>(R.id.bottom_progress)
        seekBar?.setOnSeekBarChangeListener(this)
    }

    override fun attach(mediaPlayerController: MediaPlayerController?, iViewController: IViewController) {
        this.mediaPlayerController = mediaPlayerController
        this.iViewController = iViewController
    }

    override fun getView(): View {
        return this
    }

    override fun onVisibilityChanged(isVisible: Boolean, anim: Animation?) {
        LogUtils.d(TAG, "onVisibilityChanged  $isVisible ")

        if (isVisible) {
            val fullScreen = mediaPlayerController?.isFullScreen()!!
            val islocked = iViewController?.isLocked()!!
            if (fullScreen && islocked) {
                bottom_progress?.visibility = GONE
                bottom_container?.visibility = GONE
            } else {
                bottom_progress?.visibility = GONE
//            bottom_container?.startAnimation(anim)
                bottom_container?.visibility = VISIBLE
            }
        } else {
            bottom_progress?.visibility = VISIBLE
            bottom_container?.visibility = GONE

        }

    }

    override fun onPlayStateChanged(playState: Int) {
        LogUtils.d(TAG, "onPlayStateChanged", playState)
        when (playState) {
            PlayStatus.PLAYER_FULL_SCREEN -> {
            }
            PlayStatus.PLAYER_NORMAL -> {
            }
            PlayStatus.STATE_IDLE,
            PlayStatus.STATE_PLAYBACK_COMPLETED -> {
                bottom_progress?.progress = 0
                seekBar?.progress = 0
            }
            PlayStatus.STATE_START_ABORT,
            PlayStatus.STATE_ERROR,
            PlayStatus.STATE_PREPARED,
            PlayStatus.STATE_PREPARING -> {

            }
            PlayStatus.STATE_PLAYING -> {
                iViewController?.startProgress()

            }
            PlayStatus.STATE_PAUSED -> {
                iViewController?.stopProgress()
            }
            PlayStatus.STATE_BUFFERED, PlayStatus.STATE_BUFFERING -> {
                val playing = mediaPlayerController?.isPlaying()
            }
        }
    }


    override fun setProgress(duration: Long?, position: Long?) {
        LogUtils.d(TAG, "setProgress ${duration}  ${position}")

        duration?.let {
            if (bottom_progress?.max != it.toInt()) {
                total_time?.setText(TimeStrUtils.stringForTime(it))
                bottom_progress?.max = it.toInt()
                seekBar?.max = it.toInt()
            }
        }
        position?.let {
            if (isCanChage) {
                curr_time?.setText(TimeStrUtils.stringForTime(it))
                bottom_progress?.progress = it.toInt()
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    seekBar?.setProgress(it.toInt(), true)
                } else {
                    seekBar?.progress = it.toInt()
                }
            }
        }

    }

    override fun onLockStateChanged(isLocked: Boolean) {}

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.fullscreen -> {
                val isFull = mediaPlayerController?.isFullScreen()
                if (isFull == null || !isFull) {
                    mediaPlayerController?.startFullScreen(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
                } else {
                    mediaPlayerController?.stopFullScreen()
                }
            }
            R.id.iv_play -> {
                mediaPlayerController?.togglePlay()
            }
        }
    }

    override fun onProgressChanged(p0: SeekBar?, progress: Int, fromUser: Boolean) {
        if (!fromUser) {
            return
        }
        var max = seekBar?.max
        if (max != null && max > 0) {
            val duration = mediaPlayerController?.getDuration();
            duration?.let {
                var target = duration * progress / max
                curr_time?.setText(TimeStrUtils.stringForTime(target))
            }
        }

    }

    override fun onStartTrackingTouch(p0: SeekBar?) {
        isCanChage=false
    }

    override fun onStopTrackingTouch(p0: SeekBar?) {
        var max = seekBar?.max
        if (max != null && max > 0) {
            val duration = mediaPlayerController?.getDuration();
            var target = duration!! * p0?.progress!! / max
            mediaPlayerController?.seekTo(target)
        }
        isCanChage=true
    }


}