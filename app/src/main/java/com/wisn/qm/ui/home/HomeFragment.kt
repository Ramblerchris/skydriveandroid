package com.wisn.qm.ui.home

import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.Utils
import com.blankj.utilcode.util.VibrateUtils
import com.library.base.BaseFragment
import com.library.base.utils.DownloadUtils
import com.library.base.utils.MToastUtils
import com.qmuiteam.qmui.arch.QMUIFragment
import com.qmuiteam.qmui.skin.QMUISkinManager
import com.qmuiteam.qmui.util.QMUIDisplayHelper
import com.qmuiteam.qmui.util.QMUIStatusBarHelper
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import com.wisn.qm.BuildConfig
import com.wisn.qm.R
import com.wisn.qm.task.TaskUitls
import com.wisn.qm.ui.home.adapter.HomePagerAdapter
import com.wisn.qm.ui.home.controller.*
import com.wisn.qm.ui.video.TestVideoPlayerFragment
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.item_photo_select_bottom.*
import java.util.*

/**
 * Created by Wisn on 2020/4/30 下午8:03.
 */
class HomeFragment : BaseFragment<HomeViewModel>(), HomeControlListener {
    val TAG: String = "HomeFragment"
    var pictureController: PictureController? = null

    override fun initView(views: View) {
        super.initView(views)
        if(BuildConfig.DEBUG){
            startFragment(TestVideoPlayerFragment())
        }
        LogUtils.d(TAG, " HomeFragment.initView")
        showPictureControl(false)
        initTabs()
        initPager()
        TaskUitls.exeUploadRequest(Utils.getApp(), TaskUitls.buildUploadRequest())
        viewModel.checkUpdate().observe(this, Observer {
            QMUIDialog.MessageDialogBuilder(context)
                    .setTitle("更新提醒")
                    .setSkinManager(QMUISkinManager.defaultInstance(context))
                    .setMessage("更新版本号${it.buildVersion} (build${it.buildBuildVersion})")
                    .addAction("取消") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .addAction("下载") { dialog, _ ->
                        dialog.dismiss()
                        DownloadUtils.addDownload(requireContext(), it.downloadURL!!, "更新", "更新版本号${it.buildVersion} (build${it.buildBuildVersion})")
                        MToastUtils.show("已添加升级下载任务")
                    }
                    .create(R.style.QMUI_Dialog).show()
        })
    }

    override fun onBackPressed() {
        LogUtils.d(TAG, " HomeFragment.onBackPressed")
        if (Exit()) {
            super.onBackPressed()
        }
    }

    fun Exit():Boolean {
        if (item_photo_select_bottom?.visibility == View.VISIBLE) {
            showPictureControl(false)
            pictureController?.onBackPressedExit();
            return false
        }
        return true
    }

    override fun layoutId(): Int {
        return R.layout.fragment_home;
    }

    private fun initPager() {
        val pagers = HashMap<Pager, BaseHomeController>()
        pictureController = PictureController(requireContext(), this, viewModel)
        pagers[Pager.getPagerByPosition(0)] = pictureController!!
        pagers[Pager.getPagerByPosition(1)] = AlbumController(requireContext(), this, viewModel)
        pagers[Pager.getPagerByPosition(3)] = MineController(requireContext(), this, viewModel)
        with(pager) {
            this?.adapter = HomePagerAdapter(pagers)
            tabs!!.setupWithViewPager(this, false)
            this?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

                }

                override fun onPageSelected(position: Int) {
                    QMUIStatusBarHelper.setStatusBarLightMode(activity)
                    TaskUitls.exeUploadRequest(Utils.getApp(), TaskUitls.buildUploadRequest())
                }

                override fun onPageScrollStateChanged(state: Int) {

                }

            })
        }
    }


    private fun initTabs() {
//        LogUtils.d("initTabs")
        val qmuiTabBuilder = tabs!!.tabBuilder()
        qmuiTabBuilder.setSelectedIconScale(1.1f)
                .setTextSize(QMUIDisplayHelper.sp2px(context, 12), QMUIDisplayHelper.sp2px(context, 14))
                .setDynamicChangeIconColor(false)
        val picture = qmuiTabBuilder
                .setNormalDrawable(ContextCompat.getDrawable(requireContext(), R.mipmap.ic_tab_picture_normal))
                .setSelectedDrawable(ContextCompat.getDrawable(requireContext(), R.mipmap.ic_tab_picture_checked))
                .setText("相册")
                .build(context)
        val album = qmuiTabBuilder
                .setNormalDrawable(ContextCompat.getDrawable(requireContext(), R.mipmap.ic_tab_album_normal))
                .setSelectedDrawable(ContextCompat.getDrawable(requireContext(), R.mipmap.ic_tab_album_checked))
                .setText("云相册")
                .build(context)
        val mine = qmuiTabBuilder
                .setNormalDrawable(ContextCompat.getDrawable(requireContext(), R.mipmap.ic_tab_mine_normal))
                .setSelectedDrawable(ContextCompat.getDrawable(requireContext(), R.mipmap.ic_tab_mine_checked))
                .setText("我的")
                .build(context)
        tabs!!
                .addTab(picture)
                .addTab(album)
                .addTab(mine)

    }

    override fun isShareVM(): Boolean {
        return true
    }

    override fun startFragmentByView(fragment: QMUIFragment?) {
        startFragment(fragment)
    }

    override fun showPictureControl(isShow: Boolean?) {
        isShow?.let {
            with(item_photo_select_bottom) {
                this?.visibility = if (isShow) View.VISIBLE else View.GONE
                pager?.setSwipeable(!isShow)
            }
            if (isShow) {
                VibrateUtils.vibrate(10)
            }
            tabs?.visibility = if (isShow) View.INVISIBLE else View.VISIBLE

        }
    }


}