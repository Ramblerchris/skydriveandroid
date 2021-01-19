package com.wisn.qm.ui.select.selectfile

import android.content.Intent
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
import com.wisn.qm.mode.beans.FileBean
import com.wisn.qm.ui.select.SelectFileViewModel
import kotlinx.android.synthetic.main.fragment_selectfile.*
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


    override fun layoutId(): Int {
        return R.layout.fragment_selectmedia
    }

    override fun initView(views: View) {
        title = topbar?.setTitle("文件选择")!!
        title.setTextColor(Color.BLACK)
        title.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
        leftCancel = topbar?.addLeftTextButton("返回 ", R.id.topbar_right_add_button)!!
        leftCancel.setTextColor(Color.BLACK)
        leftCancel.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
        leftCancel.visibility = View.VISIBLE
        leftCancel.setOnClickListener {
            popBackStack()
        }
        rightButton = topbar?.addRightTextButton("确定 ", R.id.topbar_right_add_button)!!
        rightButton.setTextColor(Color.BLACK)
        rightButton.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
        rightButton.setOnClickListener {
            var intent = Intent()
            val value = viewModel.selectData().value
            value?.let {
                intent.putExtra("data", value)
                setFragmentResult(101, intent)
            }
            popBackStack()
        }
        linearLayoutManager = LinearLayoutManager(context)
        with(recyclerView) {
            this?.layoutManager = linearLayoutManager
            this?.adapter = mAdapter
        }

        viewModel.getFileBeanList(null).observe(this, Observer {
            mAdapter.setNewData(it)
        })

//        viewModel.selectData().observe(this, Observer {
//            if (it != null && it.size > 0) {
//                rightButton.visibility = View.VISIBLE
//                title.text = "已选中${it.size}项"
//            } else {
//                rightButton.visibility = View.GONE
//                title.text = "请选择"
//            }
//        })

    }


    override fun onBackPressed() {
        LogUtils.d(TAG, " AlbumDetailsPageingFragment.onBackPressed")
        if (viewModel.backFileList()) {
            super.onBackPressed()
        }
    }


    override fun click(position: Int, fileBean: FileBean) {
        if (fileBean.isDir) {
            viewModel.getFileBeanList(File(fileBean.filePath))
        }
    }

//    override fun changeSelectData(isAdd: Boolean, item: MediaInfo?) {
//        if (item != null) {
//            if (isAdd) {
//                viewModel.selectData().value?.add(item)
//            } else {
//                viewModel.selectData().value?.remove(item)
//            }
//            viewModel.selectData().value = viewModel.selectData().value;
//        } else {
//            title.text = "已选中${viewModel.selectData.value?.size}项"
//        }
//    }

}