package com.wisn.qm.ui.select.selectfile

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.view.View
import android.widget.Button
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.LogUtils
import com.library.base.BaseFragment
import com.qmuiteam.qmui.qqface.QMUIQQFaceView
import com.wisn.qm.R
import com.wisn.qm.mode.beans.FileBean
import com.wisn.qm.ui.select.SelectFileViewModel
import com.wisn.qm.ui.view.ViewPosition
import kotlinx.android.synthetic.main.fragment_selectfile.*
import kotlinx.android.synthetic.main.item_empty.*
import java.io.File

/**
 *
 * @Description:
 * @Author: Wisn
 * @CreateDate: 2021/1/17 下午5:32
 */
class SelectFileFragment : BaseFragment<SelectFileViewModel>(), ClickItem {
    lateinit var title: QMUIQQFaceView
    lateinit var leftCancel: Button
    lateinit var rightButton: Button
    private val mAdapter by lazy { SelectFileAdapter(this, ArrayList()) }
    lateinit var linearLayoutManager: LinearLayoutManager
    var TAG = "SelectFileFragment"
    private var lastPosition = 0
    private var lastOffset = 0

    override fun layoutId(): Int {
        return R.layout.fragment_selectmedia
    }

    override fun initView(views: View) {
        title = topbar?.setTitle("本地文件选择")!!
        title.setTextColor(Color.BLACK)
        title.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
        leftCancel = topbar?.addLeftTextButton("返回", R.id.topbar_right_add_button)!!
        leftCancel.setTextColor(Color.BLACK)
        leftCancel.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
        leftCancel.visibility = View.VISIBLE
        leftCancel.setOnClickListener {
            popBackStack()
        }
        rightButton = topbar?.addRightTextButton("确定", R.id.topbar_right_add_button)!!
        rightButton.setTextColor(Color.BLACK)
        rightButton.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
        rightButton.setOnClickListener {
            setResultData()
            super.onBackPressed()
        }
        linearLayoutManager = LinearLayoutManager(context)
        with(recyclerView) {
            this?.layoutManager = linearLayoutManager
            this?.adapter = mAdapter
        }
        empty_tip.setText("此目录没有文件")
        empty_tip.visibility = View.GONE
        viewModel.getFileBeanList(null).observe(this, Observer {
            if (it.isNullOrEmpty()) {
                empty_tip.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                empty_tip.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                mAdapter.setNewData(it)
            }
        })
        viewModel.currentViewPosition.observe(this, Observer {
            linearLayoutManager.scrollToPositionWithOffset(it.lastPosition,it.lastOffset)
        })
        recyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                try {
                    val topView: View = linearLayoutManager.getChildAt(0)!! //获取可视的第一个view
                    lastOffset = topView.top //获取与该view的顶部的偏移量
                    lastPosition = linearLayoutManager.getPosition(topView) //得到该View的数组位置
                } catch (e: Exception) {
                }

            }

        })
//        recyclerView?.setOnScrollChangeListener(object:View.OnScrollChangeListener(){
//
//        })
        viewModel.currentFileName.observe(this, Observer {
            if (it.isNullOrEmpty()) {
                title.text = "本地文件选择"
            } else {
                title.text = it
            }
        })
    }

    private fun setResultData() {
        var intent = Intent()
        val value = mAdapter.selectList
        value?.let {
            intent.putExtra("data", value)
            setFragmentResult(101, intent)
        }
    }


    override fun onBackPressed() {
        LogUtils.d(TAG, " AlbumDetailsPageingFragment.onBackPressed")
//        super.onBackPressed()
        if (viewModel.backFileList()) {
            setResultData()
            super.onBackPressed()
        }
    }


    override fun click(position: Int, fileBean: FileBean) {
        if (fileBean.isDir) {
            viewModel.setViewPosition(ViewPosition(lastPosition,lastOffset))
            viewModel.getFileBeanList(File(fileBean.filePath))
        }
    }

}