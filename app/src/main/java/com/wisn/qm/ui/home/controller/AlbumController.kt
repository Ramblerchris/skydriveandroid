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
import com.blankj.utilcode.util.VibrateUtils
import com.jeremyliao.liveeventbus.LiveEventBus
import com.library.base.utils.MToastUtils
import com.qmuiteam.qmui.skin.QMUISkinManager
import com.qmuiteam.qmui.util.QMUIDisplayHelper
import com.qmuiteam.qmui.widget.QMUITopBarLayout
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import com.qmuiteam.qmui.widget.popup.QMUIPopup
import com.qmuiteam.qmui.widget.popup.QMUIPopups
import com.wisn.qm.R
import com.wisn.qm.mode.ConstantKey
import com.wisn.qm.mode.db.beans.UserDirBean
import com.wisn.qm.ui.album.details.AlbumDetailsPageingFragment
import com.wisn.qm.ui.album.newalbum.NewAlbumFragment
import com.wisn.qm.ui.home.HomeFragment
import com.wisn.qm.ui.home.HomeViewModel
import com.wisn.qm.ui.home.adapter.OnLineAlbumAdapter
import kotlinx.android.synthetic.main.home_controller_album.view.*
import java.util.*


/**
 * Created by Wisn on 2020/6/5 下午11:25.
 */
class AlbumController(context: Context?, mhomeFragment: HomeFragment?, homeViewModel: HomeViewModel?) : BaseHomeController(context, mhomeFragment, homeViewModel), SwipeRefreshLayout.OnRefreshListener,OnLineAlbumAdapter.CallBack {
    private val topbar: QMUITopBarLayout = findViewById(R.id.topbar)
    private val mAdapter by lazy { OnLineAlbumAdapter(this) }
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
//            mHomeViewModel.getUserDirlist()
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
                item_emptya.visibility=View.VISIBLE
            } else {
                item_emptya.visibility=View.GONE
                mAdapter.setNewData(it)
            }
        })

//        mAdapter.callback=setOnItemClickListener { baseQuickAdapter: BaseQuickAdapter<*, *>, view: View, i: Int ->
//            mHomeControlListener.let {
//                val albumDetailsFragment = AlbumDetailsPageingFragment()
//                albumDetailsFragment.arguments = Bundle()
//                albumDetailsFragment.requireArguments().putSerializable(ConstantKey.albuminfo, mAdapter.getItem(i))
//                mHomeControlListener.startFragmentByView(albumDetailsFragment)
//            }
//        }
//        mAdapter.setOnItemLongClickListener(object : OnItemLongClickListener {
//            override fun onItemLongClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int): Boolean {
//                showGlobalActionPopup(view.findViewById(R.id.name),position)
//                return true
//            }
//        })
        LiveEventBus
                .get(ConstantKey.updateAlbum, Int::class.java)
                .observe(mhomeFragment!!, Observer {
                    LogUtils.d("updateAlbum")
                    mHomeViewModel.getUserDirlist()
                })
    }
    val type_Delete=1
    val type_Share=2
    val type_cancelShare=3
    val type_updateAlbumName=4

    private fun showGlobalActionPopup(v: View,index:Int, item: UserDirBean) {
        val datalist: ArrayList<String?> = ArrayList()
        val typelist: ArrayList<Int?> = ArrayList()

        if (item.isShare == 1) {
            //分享中
            if (item.isShareFromMe == 1) {
                //分享来之我的
                datalist.add("取消公开共享")
                typelist.add(type_cancelShare)
                datalist.add("修改相册名称")
                typelist.add(type_updateAlbumName)
            } else {
                //别人分享的只能修改名称
                datalist.add("修改相册名称")
                typelist.add(type_updateAlbumName)
            }
        } else {
            //未分享的
            datalist.add("公开共享")
            typelist.add(type_Share)
            datalist.add("修改相册名称")
            typelist.add(type_updateAlbumName)
            datalist.add("删除相册")
            typelist.add(type_Delete)
        }
        if (typelist.size == 0) {
            return
        }
        val adapter: ArrayAdapter<String?> = ArrayAdapter(context, R.layout.simple_list_item, datalist)
        val onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, position, l ->
            val get = typelist.get(position)
            if (get == type_updateAlbumName) {
                val item = mAdapter.getItem(index)
                item?.let {
                    val builder = QMUIDialog.EditTextDialogBuilder(context)
                    builder.setTitle("修改相册名称")
                            .setSkinManager(QMUISkinManager.defaultInstance(context))
                            .setPlaceholder("在此输入新相册名称")
                            .setDefaultText(item.filename)
                            .setInputType(InputType.TYPE_CLASS_TEXT)
                            .addAction("取消") { dialog, index -> dialog.dismiss() }
                            .addAction("确定") { dialog, index ->
                                val text: CharSequence = builder.editText.text
                                if (!TextUtils.isEmpty(text)) {
                                    dialog.dismiss()
                                    mHomeViewModel.updateUserDirName(item.id, text.toString())
                                } else {
                                    MToastUtils.show("请输入相册名称")
                                }
                            }
                    val create=builder.create(R.style.QMUI_Dialog)
                    builder.editText.selectAll()
                    create.show()
                }


            } else if (get == type_Delete) {
                val item = mAdapter.getItem(index)
                item?.let {
                    VibrateUtils.vibrate(10)
                    QMUIDialog.MessageDialogBuilder(context)
                            .setTitle("删除相册")
                            .setSkinManager(QMUISkinManager.defaultInstance(context))
                            .setMessage("确定要删除 ${it.filename} 相册吗?")
                            .addAction("取消") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .addAction("确定") { dialog, _ ->
                                dialog.dismiss()
                                mHomeViewModel.updateDirStatus(it?.id.toString(),-1).observe(mHomeFragment, Observer {
                                    if (it) {
                                        mHomeViewModel.getUserDirlist()
                                    }
                                })
                            }
                            .create(R.style.QMUI_Dialog).show()
                }

            } else if (get == type_Share) {
                val item = mAdapter.getItem(index)
                item?.let {
                    VibrateUtils.vibrate(10)
                    QMUIDialog.MessageDialogBuilder(context)
                            .setTitle("相册公开共享")
                            .setSkinManager(QMUISkinManager.defaultInstance(context))
                            .setMessage("确定要公开共享 ${it.filename} 相册给其他用户吗?")
                            .addAction("取消") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .addAction("公开共享") { dialog, _ ->
                                dialog.dismiss()
                                mHomeViewModel.updateDirStatus(it?.id.toString(),2).observe(mHomeFragment, Observer {
                                    if (it) {
                                        mHomeViewModel.getUserDirlist()
                                    }
                                })
                            }
                            .create(R.style.QMUI_Dialog).show()
                }

            } else if (get == type_cancelShare) {
                val item = mAdapter.getItem(index)
                item?.let {
                    VibrateUtils.vibrate(10)
                    QMUIDialog.MessageDialogBuilder(context)
                            .setTitle("取消相册公开共享")
                            .setSkinManager(QMUISkinManager.defaultInstance(context))
                            .setMessage("确定要取消公开 ${it.filename} 相册给其他用户吗?")
                            .addAction("取消") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .addAction("取消公开共享") { dialog, _ ->
                                dialog.dismiss()
                                mHomeViewModel.updateDirStatus(it?.id.toString(),1).observe(mHomeFragment, Observer {
                                    if (it) {
                                        mHomeViewModel.getUserDirlist()
                                    }
                                })
                            }
                            .create(R.style.QMUI_Dialog).show()
                }

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

    override fun AlbumLongClick(itemView: View, position: Int, item: UserDirBean) {
        showGlobalActionPopup(itemView.findViewById(R.id.name), position,item)

    }

    override fun AlbumClick(itemView: View, position: Int, item: UserDirBean) {
        mHomeControlListener.let {
            val albumDetailsFragment = AlbumDetailsPageingFragment()
            albumDetailsFragment.arguments = Bundle()
            albumDetailsFragment.requireArguments().putSerializable(ConstantKey.albuminfo, mAdapter.getItem(position))
            mHomeControlListener.startFragmentByView(albumDetailsFragment)
        }
    }

}