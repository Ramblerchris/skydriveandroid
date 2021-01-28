package com.wisn.qm.ui.disk

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Button
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.jeremyliao.liveeventbus.LiveEventBus
import com.library.base.BaseFragment
import com.library.base.config.Constant
import com.qmuiteam.qmui.qqface.QMUIQQFaceView
import com.qmuiteam.qmui.skin.QMUISkinManager
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import com.wisn.qm.R
import com.wisn.qm.mode.ConstantKey
import com.wisn.qm.mode.beans.FileBean
import com.wisn.qm.mode.db.beans.UserDirBean
import com.wisn.qm.ui.select.selectfile.SelectFileFragment
import com.wisn.qm.ui.view.ViewPosition
import kotlinx.android.synthetic.main.fragment_disklist.*
import kotlinx.android.synthetic.main.fragment_disklist.recyclerView
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

    private var lastPosition = 0
    private var lastOffset = 0

    override fun layoutId(): Int {
        return R.layout.fragment_disklist
    }

    override fun initView(views: View) {
        title = topbar?.setTitle("在线文件盘")!!
        title.setTextColor(Color.BLACK)
        title.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
        leftCancel = topbar?.addLeftTextButton("返回", R.id.topbar_left_add_button)!!
        leftCancel.setTextColor(Color.BLACK)
        leftCancel.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
        leftCancel.visibility = View.VISIBLE
        leftCancel.setOnClickListener {
            popBackStack()
        }
        rightButton = topbar?.addRightTextButton("更多", R.id.topbar_right_add_button)!!
        rightButton.setTextColor(Color.BLACK)
        rightButton.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
        rightButton.setOnClickListener {
           showSelectList()
        }
        swiperefresh?.setOnRefreshListener(this)

        linearLayoutManager = LinearLayoutManager(context)
        item_emptya.visibility = View.GONE
        with(recyclerView) {
            this?.layoutManager = linearLayoutManager
            this?.adapter = mAdapter
        }

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

    fun showSelectList(){
        val builder = QMUIBottomSheet.BottomListSheetBuilder(activity)
        builder.setGravityCenter(true)
                .setSkinManager(QMUISkinManager.defaultInstance(context))
                .setTitle("更多")
                .setAddCancelBtn(true)
                .setAllowDrag(true)
                .setNeedRightMark(false)
                .addItem("添加文件")
                .addItem("新建文件夹")
                .setOnSheetItemClickListener { dialog, itemView, position, tag ->
                    dialog.dismiss()
                    when(position){
                        0 -> {
                            val selectFileFragment = SelectFileFragment()
                            selectFileFragment.arguments = Bundle()
                            //selectFileFragment.requireArguments().putSerializable(ConstantKey.albuminfo, get)
                            startFragmentForResult(selectFileFragment, 100)
                        }
                        1 -> {
                            val builder = QMUIDialog.EditTextDialogBuilder(context)
                            builder.setTitle("新建文件")
                                    .setSkinManager(QMUISkinManager.defaultInstance(context))
                                    .setPlaceholder("在此输入文件名称")
                                    .setInputType(InputType.TYPE_CLASS_TEXT)
                                    .addAction("取消") { dialog, index -> dialog.dismiss() }
                                    .addAction("确定") { dialog, index ->
                                        val text: CharSequence = builder.editText.text
                                        if (text != null && text.length > 0) {
                                            dialog.dismiss()
                                            viewModel.addUserDir(text.toString()).observe(this, Observer {
                                                ToastUtils.showShort("添加文件成功")
                                            })
                                        } else {
                                            ToastUtils.showShort("请输入文件名称")
                                        }
                                    }
                                    .create(R.style.QMUI_Dialog).show()
                        }
                    }

                }.build().show()
    }

    override fun onFragmentResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onFragmentResult(requestCode, resultCode, data)
        try {
            var list = data?.extras?.getSerializable("data")
            LogUtils.d("onFragmentResult", list)
            viewModel.saveFileBeanList(list as ArrayList<FileBean>)
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
            viewModel.setViewPosition(ViewPosition(lastPosition,lastOffset))
            viewModel.getDiskDirlist(fileBean.id)
        }
    }

    override fun onRefresh() {
        viewModel.refresh()
    }
}