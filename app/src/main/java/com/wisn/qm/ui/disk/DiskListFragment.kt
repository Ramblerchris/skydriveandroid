package com.wisn.qm.ui.disk

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.blankj.utilcode.util.LogUtils
import com.jeremyliao.liveeventbus.LiveEventBus
import com.library.base.BaseFragment
import com.library.base.config.Constant
import com.qmuiteam.qmui.qqface.QMUIQQFaceView
import com.wisn.qm.R
import com.wisn.qm.mode.ConstantKey
import com.wisn.qm.mode.beans.FileBean
import com.wisn.qm.mode.db.beans.UserDirBean
import com.wisn.qm.ui.select.selectfile.SelectFileFragment
import kotlinx.android.synthetic.main.fragment_disklist.*
import kotlinx.android.synthetic.main.fragment_disklist.recyclerView
import kotlinx.android.synthetic.main.fragment_disklist.swiperefresh
import kotlinx.android.synthetic.main.fragment_disklist.topbar
import kotlinx.android.synthetic.main.item_empty.*

/**
 *
 * @Description:
 * @Author: Wisn
 * @CreateDate: 2021/1/15 下午7:43
 */
class DiskListFragment : BaseFragment<DiskViewModel>(), ClickItem, SwipeRefreshLayout.OnRefreshListener {
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
            val selectFileFragment = SelectFileFragment()
            selectFileFragment.arguments = Bundle()
//            selectFileFragment.requireArguments().putSerializable(ConstantKey.albuminfo, get)
            startFragmentForResult(selectFileFragment, 100)
        }
        swiperefresh?.setOnRefreshListener(this)

        linearLayoutManager = LinearLayoutManager(context)
        item_emptya.visibility = View.GONE
        with(recyclerView) {
            this?.layoutManager = linearLayoutManager
            this?.adapter = mAdapter
        }
        empty_tip.setText("云盘为空,快去添吧！")
        viewModel.getDiskDirlist(-1).observe(this, Observer {
            swiperefresh?.isRefreshing = false
            if (it.isNullOrEmpty()) {
                item_emptya.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                item_emptya.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                mAdapter.setNewData(it)
            }
        })
        LiveEventBus
                .get(ConstantKey.updateDiskList, Int::class.java)
                .observe(this, Observer {
                    LogUtils.d("updateDiskList")
                    viewModel.refresh()
                })
    }

    override fun onFragmentResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onFragmentResult(requestCode, resultCode, data)
        try {
            var list = data?.extras?.getSerializable("data") as ArrayList<FileBean>
            LogUtils.d("onFragmentResult", list)
            viewModel.saveFileBeanList(list)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onBackPressed() {
        LogUtils.d(TAG, " DiskListFragment.onBackPressed")
        if (viewModel.backFileList()) {
            super.onBackPressed()
        }
    }

    override fun click(position: Int, fileBean: UserDirBean) {
        if (fileBean.type == Constant.TypeDir) {
            viewModel.getDiskDirlist(fileBean.id)
        }
    }

    override fun onRefresh() {
        viewModel.refresh()
    }
}