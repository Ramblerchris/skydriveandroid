package com.wisn.qm.ui.preview.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.widget.ImageView
import com.we.player.R
import com.we.player.controller.BaseViewController
import com.we.player.controller.component.*
import com.we.player.player.PlayStatus

/**
 *
 * @Description:
 * @Author: Wisn
 * @CreateDate: 2020/11/15 上午10:39
 */
class NetListVideoController : BaseViewController, View.OnClickListener {
    var lock_left: ImageView? = null
    var lock_right: ImageView? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : super(context, attributeSet, defStyleAttr) {
        addIViewItemControllerOne(ErrorControlView(context))
        addIViewItemControllerOne(PlayNetControlView(context))
        lock_left = findViewById(R.id.lock_left)
        lock_right = findViewById(R.id.lock_right)
        lock_left?.setOnClickListener(this)
        lock_right?.setOnClickListener(this)
        lock_left?.visibility = GONE
        lock_right?.visibility = GONE
    }

    override fun onVisibilityChanged(isVisible: Boolean, anim: Animation?) {
        //必须是全屏幕的时候才有锁定
        if (isVisible && mediaPlayerController?.isFullScreen()!!) {
            if (islock) {
                lock_left?.visibility = VISIBLE
                lock_right?.visibility = VISIBLE
            } else {
                lock_left?.visibility = GONE
                lock_right?.visibility = VISIBLE
            }

        } else {
            lock_left?.visibility = GONE
            lock_right?.visibility = GONE
        }
    }

    override fun onLockStateChanged(isLocked: Boolean) {
        super.onLockStateChanged(isLocked)
        if (islock) {
            lock_left?.isSelected = true
            lock_right?.isSelected = true
        } else {
            lock_left?.isSelected = false
            lock_right?.isSelected = false
        }
    }

    override fun setPlayStatus(status: Int) {
        super.setPlayStatus(status)
        if (status == PlayStatus.PLAYER_NORMAL) {
            lock_left?.visibility = GONE
            lock_right?.visibility = GONE
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.item_controller_standard
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.lock_right, R.id.lock_left -> {
                setLocked(!islock)
            }
        }
    }

    override fun onBackPressed(): Boolean {
        mediaPlayerController?.let {
            if (it.isFullScreen()) {
                it.stopFullScreen()
                return true
            }
        }
        return super.onBackPressed()
    }


}
