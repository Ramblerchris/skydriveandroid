package com.wisn.qm.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.Observer
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.Utils
import com.jeremyliao.liveeventbus.LiveEventBus
import com.library.base.BaseFragmentActivity
import com.library.base.utils.UploadTip
import com.qmuiteam.qmui.arch.annotation.DefaultFirstFragment
import com.qmuiteam.qmui.arch.annotation.FirstFragments
import com.qmuiteam.qmui.arch.annotation.LatestVisitRecord
import com.qmuiteam.qmui.layout.QMUIFrameLayout
import com.qmuiteam.qmui.skin.QMUISkinHelper
import com.qmuiteam.qmui.skin.QMUISkinValueBuilder
import com.qmuiteam.qmui.util.QMUIDisplayHelper
import com.qmuiteam.qmui.util.QMUIResHelper
import com.qmuiteam.qmui.util.QMUIStatusBarHelper
import com.qmuiteam.qmui.util.QMUIViewOffsetHelper
import com.qmuiteam.qmui.widget.QMUIProgressBar
import com.wisn.qm.R
import com.wisn.qm.mode.ConstantKey
import com.wisn.qm.task.TaskUitls
import com.wisn.qm.task.UploadCountProgress
import com.wisn.qm.ui.home.HomeFragment

@FirstFragments(value = [HomeFragment::class])
@DefaultFirstFragment(HomeFragment::class)
@LatestVisitRecord
open class MainActivity : BaseFragmentActivity<MainViewModel>() {
    private val storagePermissions: Array<String> = arrayOf("android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.ACCESS_WIFI_STATE");
    private lateinit var  customRootView: CustomRootView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityCompat.requestPermissions(this, storagePermissions, 1)
        QMUIStatusBarHelper.setStatusBarLightMode(this)
        LiveEventBus
                .get(ConstantKey.uploadingInfo, UploadCountProgress::class.java)
                .observe(this, Observer {
                    var progress:Int = ((it.sum-it.leftsize).toDouble()/it.sum.toDouble()*100).toInt()

                    Log.d("AAABBBCCC"," ${progress} ${it.toString()}")
                    if(it.isFinish){
                        customRootView.globalBtn.visibility = View.VISIBLE
                        customRootView.progressInfo.text = "上传完成"
                    }else{
                        if(customRootView.globalBtn.visibility==View.GONE){
                            customRootView.circleProgressBar.setProgress(progress,false)
                            customRootView.globalBtn.visibility = View.VISIBLE
                        }
                        customRootView.circleProgressBar.setProgress(progress,true)
                        customRootView.progressInfo.text ="${progress}%"
                    }
                })
        LiveEventBus
                .get(ConstantKey.updatePhotoList, Int::class.java)
                .observe(this, Observer {
                    LogUtils.d("updatePhotoList")
                    UploadTip.tipVibrate(60)
                    customRootView.globalBtn.visibility = View.GONE
                    customRootView.circleProgressBar.setProgress(0,false)

                })
    }

    override fun onBackPressed() {
        TaskUitls.exeUploadRequest(Utils.getApp(), TaskUitls.buildUploadRequest())
        LogUtils.d(" mBackStack(backStackEntryCount):"+supportFragmentManager.backStackEntryCount)
        LogUtils.d(" mBackStack(fragments size):"+supportFragmentManager.fragments.size)
        val get = supportFragmentManager.fragments.get(0)
        if (get is HomeFragment && !get.Exit()) {
            return
        }
        if (supportFragmentManager.backStackEntryCount <= 1 ) {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }else{
            super.onBackPressed()
        }
    }


    override fun onCreateRootView(fragmentContainerId: Int): RootView {
         customRootView=  CustomRootView(this, fragmentContainerId);
//        customRootView.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS)
        return customRootView
    }


    internal inner class CustomRootView(context: Context?, fragmentContainerId: Int) : RootView(context, fragmentContainerId) {
        private val fragmentContainer: FragmentContainerView
        val globalBtn: QMUIFrameLayout
        val progressInfo: TextView
        val circleProgressBar: QMUIProgressBar
        private val globalBtnOffsetHelper: QMUIViewOffsetHelper
        private val btnSize: Int
        private val touchSlop: Int
        private var touchDownX = 0f
        private var touchDownY = 0f
        private var lastTouchX = 0f
        private var lastTouchY = 0f
        private var isDragging = false
        private var isTouchDownInGlobalBtn = false
        override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
            super.onLayout(changed, left, top, right, bottom)
            globalBtnOffsetHelper.onViewLayout()
        }

        override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
            val x = event.x
            val y = event.y
            val action = event.action
            if (action == MotionEvent.ACTION_DOWN) {
                isTouchDownInGlobalBtn = isDownInGlobalBtn(x, y)
                lastTouchX = x
                touchDownX = lastTouchX
                lastTouchY = y
                touchDownY = lastTouchY
            } else if (action == MotionEvent.ACTION_MOVE) {
                if (!isDragging && isTouchDownInGlobalBtn) {
                    val dx = (x - touchDownX).toInt()
                    val dy = (y - touchDownY).toInt()
                    if (Math.sqrt(dx * dx + dy * dy.toDouble()) > touchSlop) {
                        isDragging = true
                    }
                }
                if (isDragging) {
                    var dx = (x - lastTouchX).toInt()
                    var dy = (y - lastTouchY).toInt()
                    val gx = globalBtn.left
                    val gy = globalBtn.top
                    val gw = globalBtn.width
                    val w = width
                    val gh = globalBtn.height
                    val h = height
                    if (gx + dx < 0) {
                        dx = -gx
                    } else if (gx + dx + gw > w) {
                        dx = w - gw - gx
                    }
                    if (gy + dy < 0) {
                        dy = -gy
                    } else if (gy + dy + gh > h) {
                        dy = h - gh - gy
                    }
                    globalBtnOffsetHelper.leftAndRightOffset = globalBtnOffsetHelper.leftAndRightOffset + dx
                    globalBtnOffsetHelper.topAndBottomOffset = globalBtnOffsetHelper.topAndBottomOffset + dy
                }
                lastTouchX = x
                lastTouchY = y
            } else if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
                isDragging = false
                isTouchDownInGlobalBtn = false
            }
            return isDragging
        }

        private fun isDownInGlobalBtn(x: Float, y: Float): Boolean {
            return globalBtn.left < x && globalBtn.right > x && globalBtn.top < y && globalBtn.bottom > y
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            val x = event.x
            val y = event.y
            val action = event.action
            if (action == MotionEvent.ACTION_DOWN) {
                isTouchDownInGlobalBtn = isDownInGlobalBtn(x, y)
                lastTouchX = x
                touchDownX = lastTouchX
                lastTouchY = y
                touchDownY = lastTouchY
            } else if (action == MotionEvent.ACTION_MOVE) {
                if (!isDragging && isTouchDownInGlobalBtn) {
                    val dx = (x - touchDownX).toInt()
                    val dy = (y - touchDownY).toInt()
                    if (Math.sqrt(dx * dx + dy * dy.toDouble()) > touchSlop) {
                        isDragging = true
                    }
                }
                if (isDragging) {
                    var dx = (x - lastTouchX).toInt()
                    var dy = (y - lastTouchY).toInt()
                    val gx = globalBtn.left
                    val gy = globalBtn.top
                    val gw = globalBtn.width
                    val w = width
                    val gh = globalBtn.height
                    val h = height
                    if (gx + dx < 0) {
                        dx = -gx
                    } else if (gx + dx + gw > w) {
                        dx = w - gw - gx
                    }
                    if (gy + dy < 0) {
                        dy = -gy
                    } else if (gy + dy + gh > h) {
                        dy = h - gh - gy
                    }
                    globalBtnOffsetHelper.leftAndRightOffset = globalBtnOffsetHelper.leftAndRightOffset + dx
                    globalBtnOffsetHelper.topAndBottomOffset = globalBtnOffsetHelper.topAndBottomOffset + dy
                }
                lastTouchX = x
                lastTouchY = y
            } else if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
                isDragging = false
                isTouchDownInGlobalBtn = false
            }
            return isDragging || super.onTouchEvent(event)
        }

        override fun getFragmentContainerView(): FragmentContainerView {
            return fragmentContainer
        }

        init {
            btnSize = QMUIDisplayHelper.dp2px(context, 56)
            fragmentContainer = FragmentContainerView(context!!)
            fragmentContainer.id = fragmentContainerId
//            fragmentContainer.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            fragmentContainer.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
//                for (i in 0 until childCount) {
////                    SwipeBackLayout.updateLayoutInSwipeBack(getChildAt(i))
//                }
            }
            addView(fragmentContainer, LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
            globalBtn =
                LayoutInflater.from(context).inflate(R.layout.item_main_progress_tip, null) as QMUIFrameLayout
            progressInfo=globalBtn.findViewById<TextView>(R.id.name)
            circleProgressBar=globalBtn.findViewById<QMUIProgressBar>(R.id.circleProgressBar)
            circleProgressBar?.setMaxValue(100)
//            globalBtn.setBackgroundResource(R.mipmap.ic_launcher)
//            globalBtn.scaleType = ImageView.ScaleType.CENTER_INSIDE
//            globalBtn.setRadiusAndShadow(btnSize / 2,
//                    QMUIDisplayHelper.dp2px(getContext(), 16), 0.4f)
//            globalBtn.borderWidth = 1
//            globalBtn.borderColor = QMUIResHelper.getAttrColor(context, R.attr.qmui_skin_support_color_separator)
//            globalBtn.setRadiusAndShadow(btnSize / 2,
//                    QMUIDisplayHelper.dp2px(getContext(), 16), 0.4f)
//            globalBtn.setBackgroundColor(QMUIResHelper.getAttrColor(context, R.attr.app_skin_common_background))
////            globalBtn.setOnClickListener {
////                //                    showGlobalActionPopup(v);
////            }
//            globalBtn.visibility= View.GONE
            val globalBtnLp = LayoutParams(btnSize, btnSize)
            globalBtnLp.gravity = Gravity.BOTTOM or Gravity.RIGHT
            globalBtnLp.bottomMargin = QMUIDisplayHelper.dp2px(context, 60)
            globalBtnLp.rightMargin = QMUIDisplayHelper.dp2px(context, 24)
            globalBtn.setRadiusAndShadow(btnSize / 2,
                    QMUIDisplayHelper.dp2px(getContext(), 16), 0.4f)
            val builder = QMUISkinValueBuilder.acquire()
            builder.background(R.attr.app_skin_common_background)
            builder.border(R.attr.qmui_skin_support_color_separator)
            builder.tintColor(R.attr.app_skin_common_img_tint_color)
            QMUISkinHelper.setSkinValue(globalBtn, builder)
            builder.release()
//           globalBtn.visibility= View.VISIBLE
//           globalBtn.resources.configuration.fontScale=1f
//           globalBtn.text = "测试"
            addView(globalBtn, globalBtnLp)
            globalBtn.visibility=View.GONE
            globalBtnOffsetHelper = QMUIViewOffsetHelper(globalBtn)
            touchSlop = ViewConfiguration.get(context).scaledTouchSlop
        }
    }

    override fun overridePendingTransition(enterAnim: Int, exitAnim: Int) {
        super.overridePendingTransition(0, 0)
    }

}