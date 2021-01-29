package com.wisn.qm.ui.home.controller

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.blankj.utilcode.util.VibrateUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemLongClickListener
import com.jeremyliao.liveeventbus.LiveEventBus
import com.qmuiteam.qmui.skin.QMUISkinManager
import com.qmuiteam.qmui.util.QMUIDisplayHelper
import com.qmuiteam.qmui.widget.QMUITopBarLayout
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import com.qmuiteam.qmui.widget.popup.QMUIPopup
import com.qmuiteam.qmui.widget.popup.QMUIPopups
import com.wisn.qm.R
import com.wisn.qm.mode.ConstantKey
import com.wisn.qm.ui.album.details.AlbumDetailsPageingFragment
import com.wisn.qm.ui.album.newalbum.NewAlbumFragment
import com.wisn.qm.ui.home.HomeFragment
import com.wisn.qm.ui.home.HomeViewModel
import com.wisn.qm.ui.home.adapter.AlbumAdapter
import java.util.*


/**
 * Created by Wisn on 2020/6/5 下午11:25.
 */
class AlbumController(context: Context?, mhomeFragment: HomeFragment?, homeViewModel: HomeViewModel?) : BaseHomeController(context, mhomeFragment, homeViewModel), SwipeRefreshLayout.OnRefreshListener {
    private val topbar: QMUITopBarLayout = findViewById(R.id.topbar)
    private val mAdapter by lazy { AlbumAdapter() }
    private val recyclerView: RecyclerView
    private val swiperefresh: SwipeRefreshLayout
    var mGlobalAction: QMUIPopup? =null

    override val layoutId: Int
        get() = R.layout.home_controller_album

    init {
        val Leftbutton = topbar.setTitle("云相册")
        Leftbutton.setTextColor(Color.BLACK)
        Leftbutton.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

        val addfile = topbar.addRightTextButton("新建", R.id.topbar_right_add_button)
        addfile.setTextColor(Color.BLACK)
        addfile.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
        addfile.setOnClickListener {
            mHomeControlListener.let {
                mHomeControlListener.startFragmentByView(NewAlbumFragment())
            }
            mHomeViewModel.getUserDirlist()
        }
        recyclerView = findViewById(R.id.recyclerView)
        swiperefresh = findViewById(R.id.swiperefresh)
        with(recyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }
        swiperefresh?.setOnRefreshListener(this)

        mHomeViewModel.getUserDirlist().observe(mhomeFragment!!, Observer {
            swiperefresh?.isRefreshing = false
            LogUtils.d("updateAlbum!!!!!!!!!!!!!!!!!!!!!!!!!!1")
            if (it.isNullOrEmpty()) {
                mAdapter.setEmptyView(R.layout.item_empty)
            } else {
                mAdapter.setNewInstance(it)
            }
        })
        mAdapter.setOnItemClickListener { baseQuickAdapter: BaseQuickAdapter<*, *>, view: View, i: Int ->
            mHomeControlListener.let {
                val albumDetailsFragment = AlbumDetailsPageingFragment()
                albumDetailsFragment.arguments = Bundle()
                albumDetailsFragment.requireArguments().putSerializable(ConstantKey.albuminfo, mAdapter.getItem(i))
                mHomeControlListener.startFragmentByView(albumDetailsFragment)
            }
        }
        mAdapter.setOnItemLongClickListener(object : OnItemLongClickListener {
            override fun onItemLongClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int): Boolean {
                showGlobalActionPopup(view.findViewById(R.id.name),position)
                return true
            }

        })
        LiveEventBus
                .get(ConstantKey.updateAlbum, Int::class.java)
                .observe(mhomeFragment!!, Observer {
                    LogUtils.d("updateAlbum")
                    mHomeViewModel.getUserDirlist()
                })
    }

    private fun showGlobalActionPopup(v: View,index:Int) {
        val datalist: ArrayList<String?> = ArrayList()
        datalist.add("修改相册名称")
        datalist.add("删除相册")
        val adapter: ArrayAdapter<String?> = ArrayAdapter(context, R.layout.simple_list_item, datalist)
        val onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, position, l ->
            if (position == 0) {
                val item = mAdapter.getItem(index)
                val builder = QMUIDialog.EditTextDialogBuilder(context)
                builder.setTitle("修改相册名称")
                        .setSkinManager(QMUISkinManager.defaultInstance(context))
                        .setPlaceholder("在此输入相册名称")
                        .setDefaultText(item.filename)
                        .setInputType(InputType.TYPE_CLASS_TEXT)
                        .addAction("取消") { dialog, index -> dialog.dismiss() }
                        .addAction("确定") { dialog, index ->
                            val text: CharSequence = builder.editText.text
                            if (!TextUtils.isEmpty(text)) {
                                dialog.dismiss()
                                mHomeViewModel.updateUserDirName(item.id, text.toString())
                            } else {
                                ToastUtils.showShort("请输入相册名称")
                            }
                        }

                val create=builder.create(R.style.QMUI_Dialog)
                create.setOnShowListener {
                    builder.editText.selectAll()
                }
                create.show()

            } else if (position == 1) {
                val item = mAdapter.getItem(index)
                VibrateUtils.vibrate(10)
                QMUIDialog.MessageDialogBuilder(context)
                        .setTitle("删除相册")
                        .setSkinManager(QMUISkinManager.defaultInstance(context))
                        .setMessage("确定要删除 ${item.filename} 相册吗?")
                        .addAction("取消") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .addAction("确定") { dialog, _ ->
                            dialog.dismiss()
                            mHomeViewModel.deleteDirs(item.id.toString()).observe(mHomeFragment, Observer {
                                if (it) {
                                    mHomeViewModel.getUserDirlist()
                                }
                            })
                        }
                        .create(R.style.QMUI_Dialog).show()
            }
            mGlobalAction?.dismiss()
        }
        mGlobalAction = QMUIPopups.listPopup(context,
                QMUIDisplayHelper.dp2px(context, 250),
                QMUIDisplayHelper.dp2px(context, 300),
                adapter,
                onItemClickListener)
                .animStyle(QMUIPopup.ANIM_GROW_FROM_CENTER)
                .preferredDirection(QMUIPopup.DIRECTION_TOP)
                .shadow(true)
                .dimAmount(0.3f)
                .edgeProtection(QMUIDisplayHelper.dp2px(context, 10))
                .offsetYIfTop(QMUIDisplayHelper.dp2px(context, 5))
                .skinManager(QMUISkinManager.defaultInstance(context))
                .show(v)
    }

    override fun onRefresh() {
        mHomeViewModel.getUserDirlist()
    }
}