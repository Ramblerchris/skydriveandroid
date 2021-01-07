package com.wisn.qm.ui.album.details

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.blankj.utilcode.util.LogUtils
import com.jeremyliao.liveeventbus.LiveEventBus
import com.library.base.BaseFragment
import com.qmuiteam.qmui.qqface.QMUIQQFaceView
import com.qmuiteam.qmui.skin.QMUISkinManager
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import com.wisn.qm.R
import com.wisn.qm.mode.ConstantKey
import com.wisn.qm.mode.beans.FileType
import com.wisn.qm.mode.db.beans.UserDirBean
import com.wisn.qm.mode.db.beans.MediaInfo
import com.wisn.qm.ui.album.AlbumViewModel
import com.wisn.qm.ui.album.EditAlbumDetails
import com.wisn.qm.ui.netpreview.NetPreviewFragment
import com.wisn.qm.ui.selectpic.SelectPictureFragment
import kotlinx.android.synthetic.main.fragment_albumdetails.*
import kotlinx.android.synthetic.main.item_empty.view.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.lang.Exception


class AlbumDetailsPageingFragment : BaseFragment<AlbumViewModel>(), SwipeRefreshLayout.OnRefreshListener, EditAlbumDetails {
    lateinit var title: QMUIQQFaceView
    lateinit var rightButton: Button
    var isShowEdit: Boolean = false

    var TAG = "AlbumDetailsPageingFragment"

    private val albumPictureAdapter by lazy {
        AlbumDetailsPageingAdapter(this, this) { i: Int, it: UserDirBean?, albumDetailsPageingAdapter: AlbumDetailsPageingAdapter ->
//            it?. = "黄林晴${position}"
            Log.d(TAG, "getUserDirListDataSource update")
            albumDetailsPageingAdapter.notifyDataSetChanged()
        }
    }
    val get by lazy { arguments?.get(ConstantKey.albuminfo) as UserDirBean }

    override fun layoutId(): Int {
        return R.layout.fragment_albumdetails
    }

    override fun initView(views: View) {
        super.initView(views)
        title = topbar.setTitle("相册")!!
        title.setTextColor(Color.BLACK)
        title.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
        val addLeftBackImageButton = topbar?.addLeftBackImageButton()
        addLeftBackImageButton?.setColorFilter(Color.BLACK)
        addLeftBackImageButton?.setOnClickListener {
            if (isShowEdit) {
                albumPictureAdapter.updateSelect(false)
            } else {
                popBackStack()
            }
        }

        rightButton = topbar?.addRightTextButton("添加 ", R.id.topbar_right_add_button)!!
        rightButton.setTextColor(Color.BLACK)
        rightButton.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
        rightButton.setOnClickListener {
            if (isShowEdit) {
                QMUIDialog.MessageDialogBuilder(context)
                        .setTitle("确定要删除吗？")
                        .setSkinManager(QMUISkinManager.defaultInstance(context))
//                        .setMessage("确定要删除${item.filename}相册吗?")
                        .addAction("取消") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .addAction("确定") { dialog, _ ->
                            dialog.dismiss()
                            viewModel.deletefiles(get.id)
                        }
                        .create(R.style.QMUI_Dialog).show()
            } else {
                val selectPictureFragment = SelectPictureFragment()
                selectPictureFragment.arguments = Bundle()
                selectPictureFragment.requireArguments().putSerializable(ConstantKey.albuminfo, get)
                startFragmentForResult(selectPictureFragment, 100)
            }
        }
        swiperefresh?.setOnRefreshListener(this)
        var gridLayoutManager = GridLayoutManager(context, 3)
        with(gridLayoutManager) {
            spanSizeLookup = SpanSizeLookupPage(albumPictureAdapter)
        }

        with(recyclerView!!) {
            adapter = albumPictureAdapter.withLoadStateFooter(footer = LoadStateFooterAdapter(retry = {
                albumPictureAdapter.retry()
            }))
            layoutManager = gridLayoutManager
        }
        title.text = get.filename


        viewModel.selectData().observe(this, Observer {
            LogUtils.d(" mHomeViewModel.selectData")
            if (isShowEdit) {
                title.text = "已选中${it?.size}项"
            }

        })
        LiveEventBus
                .get(ConstantKey.updatePhotoList, Int::class.java)
                .observe(this, Observer {
                    LogUtils.d("updatePhotoList")
                    albumPictureAdapter.refresh()
                })
        albumPictureAdapter.addLoadStateListener {
            Log.d(TAG, "it.source addLoadStateListener " + it.source)
            Log.d(TAG, "it.prepend addLoadStateListener " + it.prepend)
            Log.d(TAG, "it.append addLoadStateListener " + it.append)
            Log.d(TAG, "it.refresh addLoadStateListener " + it.refresh)
            when (it.refresh) {
                is LoadState.NotLoading -> {
                    Log.d(TAG, "is NotLoading")
                }
                is LoadState.Error -> {
                    if ((it.refresh as LoadState.Error).error is EmptyDataError) {
//                        Log.d("LoadStateViewHolder", "--"+loadState)
                        item_empty.image.setImageResource(R.mipmap.share_ic_blank_album)
                        item_empty.empty_tip.setText("相册为空,快去添吧！")
                        swiperefresh.visibility = View.GONE
                        item_empty.visibility = View.VISIBLE
                    }
                }
                is LoadState.Loading -> {
                    Log.d(TAG, "Loading")

                }
            }
        }
        swiperefresh.isRefreshing = true
        lifecycleScope.launch {
            try {
                viewModel.getUserDirListDataSource(get.id).collectLatest {
                    Log.d(TAG, "AAAAAgetUserDirListDataSource ")
                    swiperefresh?.isRefreshing = false
                    swiperefresh.visibility = View.VISIBLE
                    item_empty.visibility = View.GONE
                    albumPictureAdapter.submitData(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                LogUtils.d(TAG, "getUserDirListDataSource：" + e)
            }
        }
    }

    override fun onFragmentResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onFragmentResult(requestCode, resultCode, data)
        var list = data?.extras?.getSerializable("data") as ArrayList<MediaInfo>
        LogUtils.d("onFragmentResult", list)
        viewModel.saveMedianInfo(list, get)
    }

    override fun onRefresh() {
        albumPictureAdapter.refresh()
//        viewModel.getUserDirlist(get.id)
        LogUtils.d("onRefresh")

    }

    override fun isShowEdit(isShow: Boolean) {
        this.isShowEdit = isShow
        if (isShow) {
            title.text = "已选中${viewModel.selectData.value?.size ?: 0}项"
            rightButton.text = "删除"
        } else {
            title.text = get.filename
            rightButton.text = "添加"
        }
    }

    override fun changeSelectData(isinit: Boolean, isAdd: Boolean, userDirBean: UserDirBean?) {
        viewModel.editUserDirBean(isinit, isAdd, userDirBean)

    }
    open fun prePic(position: Int) {
        //查看大图
        val netPreviewFragment = NetPreviewFragment(this.viewModel.getDirListAll(), position)
        startFragment(netPreviewFragment)
    }

}

open class SpanSizeLookupPage(var adapterV2: AlbumDetailsPageingAdapter) : GridLayoutManager.SpanSizeLookup() {
    override fun getSpanSize(position: Int): Int {
        if (adapterV2.itemCount > position) {
            return when (adapterV2.getItemViewType(position)) {
                FileType.VideoViewItem,
                FileType.ImageViewItem -> 1
                FileType.TimeTitle -> 3
                else -> 3
            }
        } else {
            return 3
        }

    }

}
