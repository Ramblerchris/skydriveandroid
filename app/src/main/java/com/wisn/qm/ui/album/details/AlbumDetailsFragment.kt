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
import com.wisn.qm.mode.db.beans.UserDirBean
import com.wisn.qm.mode.db.beans.MediaInfo
import com.wisn.qm.ui.album.AlbumViewModel
import com.wisn.qm.ui.album.EditAlbumDetails
import com.wisn.qm.ui.album.bean.LoadAlbumListResult
import com.wisn.qm.ui.select.selectmedia.SelectMediaFragment
import com.wisn.qm.ui.view.LoadMoreAndFooterView
import kotlinx.android.synthetic.main.fragment_albumdetails.*
import kotlinx.android.synthetic.main.item_empty.view.*


class AlbumDetailsFragment : BaseFragment<AlbumViewModel>(), SwipeRefreshLayout.OnRefreshListener,
    EditAlbumDetails {
    lateinit var title: QMUIQQFaceView
    lateinit var rightButton: Button
    var isShowEdit: Boolean = false
    var titleSub: String = "";
    var total: Int =0;

    var gridLayoutManager: GridLayoutManager? = null;

    val albumPictureAdapter by lazy {
        gridLayoutManager = GridLayoutManager(context, 3)
        AlbumDetailsAdapter(gridLayoutManager!!, this, this)
    }
    val albuminfo by lazy { arguments?.get(ConstantKey.albuminfo) as UserDirBean }

    override fun layoutId(): Int {
        return R.layout.fragment_albumdetails
    }

    override fun initView(views: View) {
        super.initView(views)
        title = topbar.setTitle("相册")!!
        title.setTextColor(Color.BLACK)
        title.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
        titleSub= albuminfo.filename.toString()
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
                        viewModel.deleteOnlinefiles(albuminfo.id)
                    }
                    .create(R.style.QMUI_Dialog).show()
            } else {
                val selectPictureFragment = SelectMediaFragment()
                selectPictureFragment.arguments = Bundle()
                selectPictureFragment.requireArguments().putSerializable(ConstantKey.albuminfo, albuminfo)
                startFragmentForResult(selectPictureFragment, 100)
            }
        }
        swiperefresh?.setOnRefreshListener(this)
        albumPictureAdapter.loadMoreModule.setOnLoadMoreListener {
            viewModel.getloadAlbumListResult(albuminfo.id, false)
        }
        albumPictureAdapter.loadMoreModule.loadMoreView = LoadMoreAndFooterView()
        albumPictureAdapter.loadMoreModule.isAutoLoadMore = true
        albumPictureAdapter.loadMoreModule.isEnableLoadMoreIfNotFullPage = false
        with(recyclerView!!) {
            adapter = albumPictureAdapter
            layoutManager = gridLayoutManager
        }
        title.text = titleSub
        viewModel.getloadAlbumListResult(albuminfo.id, true).observe(this, Observer {
            albumPictureAdapter.loadMoreModule.isEnableLoadMore = true
            swiperefresh?.isRefreshing = false
            //删除更新
            if (it.isDeleteUpdate && it.selectSha1List != null && it.selectSha1List!!.size > 0) {
                it.selectSha1List!!.forEachIndexed { index, userDirBean ->
                    albumPictureAdapter.data.remove(userDirBean)
                }
                it.total = it.total - it.selectSha1List!!.size
                /* if(it.selectSha1List!!.size==1){
                     val indexOf = albumPictureAdapter.data.indexOf(it.selectSha1List!!.get(0))
                     albumPictureAdapter.data.removeAll(it.selectSha1List!!)
                     if(indexOf>=0){
                         albumPictureAdapter.notifyItemRemoved(indexOf)
                     }
                 }else{
                     albumPictureAdapter.data.removeAll(it.selectSha1List!!)
                     albumPictureAdapter.notifyDataSetChanged()
                 }*/

                return@Observer
            }
            if (it.total > 0) {
                total = it.total
            }
            if (it.isFirstPage) {
                if (it.loadMoreStatus == LoadAlbumListResult.LoadMore_error) {
                    setEmptyViewOrErrorView(true, "加载出错，点击重试！")
//                    albumPictureAdapter.setEmptyView(setEmptyViewOrErrorView(true,"加载出错，点击重试！"))
                } else {
                    if (it.selectSha1List.isNullOrEmpty()) {
                        setEmptyViewOrErrorView(true, "相册为空,快去添吧！")
//                        albumPictureAdapter.setEmptyView(setEmptyViewOrErrorView(true,"相册为空,快去添吧！"))
                    } else {
                        setEmptyViewOrErrorView(false, null)
                        albumPictureAdapter.setNewInstance(it.selectSha1List)
                    }
                }

            } else {
                if (it.loadMoreStatus == LoadAlbumListResult.LoadMore_end) {
                    albumPictureAdapter.loadMoreModule.loadMoreEnd();
                } else if (it.loadMoreStatus == LoadAlbumListResult.LoadMore_complete) {
                    albumPictureAdapter.loadMoreModule.loadMoreComplete();
                } else if (it.loadMoreStatus == LoadAlbumListResult.LoadMore_error) {
                    albumPictureAdapter.loadMoreModule.loadMoreFail();
                }
                it.selectSha1List?.let { data ->
                    albumPictureAdapter.addData(data)
                }
            }
            title.text = getTitle()

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
                viewModel.getloadAlbumListResult(albuminfo.id, true)
            })

    }

    private fun setEmptyViewOrErrorView(isShowEmpty: Boolean, message: String?) {
        if (isShowEmpty) {
            item_empty.visibility = View.VISIBLE;
            swiperefresh.visibility = View.GONE;
            item_empty.image.setImageResource(R.mipmap.share_ic_blank_album)
            message?.let {
                item_empty.empty_tip.setText(message)
            }
            item_empty.setOnClickListener {
                viewModel.getloadAlbumListResult(albuminfo.id, true)
            }
        } else {
            item_empty.visibility = View.GONE;
            swiperefresh.visibility = View.VISIBLE;
        }
    }

    override fun onFragmentResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onFragmentResult(requestCode, resultCode, data)
        var list = data?.extras?.getSerializable("data") as ArrayList<MediaInfo>
        LogUtils.d("onFragmentResult", list)
        viewModel.saveMedianInfo(list, albuminfo)
    }

    override fun onRefresh() {
        viewModel.getloadAlbumListResult(albuminfo.id, true)
    }

    override fun isShowEdit(isShow: Boolean) {
        this.isShowEdit = isShow
        if (isShow) {
            title.text = "已选中${viewModel.selectData.value?.size ?: 0}项"
            rightButton.setText("删除")
        } else {
            title.text = getTitle()
            rightButton.setText("添加")
        }
    }
    fun getTitle():String{
        if(total>0){
           return  "${titleSub}(${total})"
        }
        return titleSub;
    }


    override fun changeSelectData(isinit: Boolean, isAdd: Boolean, userDirBean: UserDirBean?) {
        viewModel.editOnlineUserDirBean(isinit, isAdd, userDirBean)

    }

}
