package com.wisn.qm.ui.album.details

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.lifecycle.Observer
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
import com.wisn.qm.ui.select.selectmedia.SelectMediaFragment
import com.wisn.qm.ui.view.LoadMoreAndFooterView
import kotlinx.android.synthetic.main.fragment_albumdetails.*
import kotlinx.android.synthetic.main.item_empty.view.*


class AlbumDetailsFragment : BaseFragment<AlbumViewModel>(), SwipeRefreshLayout.OnRefreshListener, EditAlbumDetails {
    lateinit var title: QMUIQQFaceView
    lateinit var rightButton: Button
    var isShowEdit: Boolean = false
    val albumPictureAdapter by lazy {
        AlbumDetailsAdapter(this, this)
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

        rightButton = topbar?.addRightTextButton("添加", R.id.topbar_right_add_button)!!
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
                            viewModel.deleteOnlinefiles(get.id)
                        }
                        .create(R.style.QMUI_Dialog).show()
            } else {
                val selectPictureFragment = SelectMediaFragment()
                selectPictureFragment.arguments = Bundle()
                selectPictureFragment.requireArguments().putSerializable(ConstantKey.albuminfo, get)
                startFragmentForResult(selectPictureFragment, 100)
            }
        }
        swiperefresh?.setOnRefreshListener(this)

        var gridLayoutManager = GridLayoutManager(context, 3)
        with(gridLayoutManager) {
            spanSizeLookup = SpanSizeLookup(albumPictureAdapter)
        }

        with(recyclerView!!) {
            adapter = albumPictureAdapter
            layoutManager = gridLayoutManager
        }
        albumPictureAdapter.loadMoreModule.setOnLoadMoreListener {
//            getDataList()
        }
        albumPictureAdapter.loadMoreModule.loadMoreView = LoadMoreAndFooterView()
        albumPictureAdapter.loadMoreModule.isAutoLoadMore = true
        albumPictureAdapter.loadMoreModule.isEnableLoadMoreIfNotFullPage = false
        title.text = get.filename
        viewModel.getUserOnlineDirlist(get.id).observe(this, Observer {
            albumPictureAdapter.loadMoreModule.isEnableLoadMore = true
            swiperefresh?.isRefreshing = false
            albumPictureAdapter.updateSelect(false)
            if (it.isNullOrEmpty()) {
                var item_empty: View = View.inflate(context, R.layout.item_empty, null)
                item_empty.image.setImageResource(R.mipmap.share_ic_blank_album)
                item_empty. empty_tip.setText("相册为空,快去添吧！")
                albumPictureAdapter.setEmptyView(item_empty)
            } else {
                albumPictureAdapter.setNewInstance(it)
            }
        })
        viewModel.selectOnlineData().observe(this, Observer {
            LogUtils.d(" mHomeViewModel.selectData")
            if (isShowEdit) {
                title.text = "已选中${it?.size}项"
            }

        })
        LiveEventBus
                .get(ConstantKey.updatePhotoList, Int::class.java)
                .observe(this, Observer {
                    LogUtils.d("updatePhotoList")
                    viewModel.getUserOnlineDirlist(get.id)
                })

    }

    override fun onFragmentResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onFragmentResult(requestCode, resultCode, data)
        var list = data?.extras?.getSerializable("data") as ArrayList<MediaInfo>
        LogUtils.d("onFragmentResult", list)
        viewModel.saveMedianInfo(list, get)
    }

    override fun onRefresh() {
        viewModel.getUserOnlineDirlist(get.id)
    }

    override fun isShowEdit(isShow: Boolean) {
        this.isShowEdit = isShow
        if (isShow) {
            title.text = "已选中${viewModel.selectData.value?.size ?: 0}项"
            rightButton.setText("删除")
        } else {
            title.text = get.filename
            rightButton.setText("添加")
        }
    }

    override fun changeSelectData(isinit: Boolean, isAdd: Boolean, userDirBean: UserDirBean?) {
        viewModel.editOnlineUserDirBean(isinit, isAdd, userDirBean)

    }

}

open class SpanSizeLookup(var adapterV2: AlbumDetailsAdapter) : GridLayoutManager.SpanSizeLookup() {
    override fun getSpanSize(position: Int): Int {
        if (adapterV2.data.isNotEmpty()) {
            val get = adapterV2.data[position]
            return when (get.itemType) {
                FileType.ImageViewItem -> 1
                FileType.TimeTitle -> 3
                else -> 1
            }
        }
        return 3;
    }

}
