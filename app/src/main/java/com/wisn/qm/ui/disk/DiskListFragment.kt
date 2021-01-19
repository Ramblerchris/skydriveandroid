package com.wisn.qm.ui.disk

import android.graphics.Color
import android.graphics.Typeface
import android.view.View
import android.widget.Button
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.LogUtils
import com.library.base.BaseFragment
import com.qmuiteam.qmui.qqface.QMUIQQFaceView
import com.wisn.qm.R
import com.wisn.qm.mode.db.beans.UserDirBean
import kotlinx.android.synthetic.main.fragment_disklist.*

/**
 *
 * @Description:
 * @Author: Wisn
 * @CreateDate: 2021/1/15 下午7:43
 */
class DiskListFragment: BaseFragment<DiskViewModel>(), ClickItem {
    lateinit var title: QMUIQQFaceView
    lateinit var leftCancel: Button
    lateinit var rightButton: Button
    private val mAdapter by lazy { DiskAdapter(this, ArrayList()) }
    lateinit var linearLayoutManager: LinearLayoutManager
    var TAG = "DiskListFragment"

    override fun layoutId(): Int {
        return R.layout.fragment_disklist
    }

    override fun initView(views: View) {
        title = topbar?.setTitle("在线文件盘")!!
        title.setTextColor(Color.BLACK)
        title.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
        leftCancel = topbar?.addLeftTextButton("返回 ", R.id.topbar_right_add_button)!!
        leftCancel.setTextColor(Color.BLACK)
        leftCancel.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
        leftCancel.visibility = View.VISIBLE
        leftCancel.setOnClickListener {
            popBackStack()
        }
        rightButton = topbar?.addRightTextButton("添加 ", R.id.topbar_right_add_button)!!
        rightButton.setTextColor(Color.BLACK)
        rightButton.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
        rightButton.setOnClickListener {
            popBackStack()
        }
        linearLayoutManager = LinearLayoutManager(context)
        with(recyclerView) {
            this?.layoutManager = linearLayoutManager
            this?.adapter = mAdapter
        }
        viewModel.getDiskDirlist(-1).observe(this, Observer {
            mAdapter.setNewData(it)
        })
    }

    override fun onBackPressed() {
        LogUtils.d(TAG, " DiskListFragment.onBackPressed")
//        if (viewModel.backFileList()) {
//            super.onBackPressed()
//        }
        super.onBackPressed()

    }

    override fun click(position: Int, fileBean: UserDirBean) {
    }
}