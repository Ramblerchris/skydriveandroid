package com.wisn.qm.ui.home.controller

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Parcelable
import android.util.SparseArray
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.qmuiteam.qmui.widget.QMUITopBarLayout
import com.wisn.qm.R
import com.wisn.qm.ui.home.HomeFragment
import com.wisn.qm.ui.home.HomeViewModel
import com.wisn.qm.ui.home.localalbum.LocalAlbumAdapterV2
import androidx.lifecycle.Observer
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.blankj.utilcode.util.LogUtils
import com.library.base.layoutmanager.MGridLayoutManager
import com.library.base.utils.MToastUtils
import com.qmuiteam.qmui.qqface.QMUIQQFaceView
import com.qmuiteam.qmui.util.QMUIViewHelper
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog
import com.wisn.qm.mode.beans.FileType
import com.wisn.qm.mode.db.beans.MediaInfo
import com.wisn.qm.ui.home.localalbum.PictureCallBack


/**
 * Created by Wisn on 2020/6/5 下午11:26.
 */
class PictureController(context: Context, mhomeFragment: HomeFragment, homeViewModel: HomeViewModel?) : BaseHomeController(context, mhomeFragment, homeViewModel), PictureCallBack, SwipeRefreshLayout.OnRefreshListener {
    private val topbar: QMUITopBarLayout = findViewById(R.id.topbar)
    private val mAdapter by lazy { LocalAlbumAdapterV2(this) }
    private var gridLayoutManager: GridLayoutManager
    private val recyclerView: RecyclerView
    private val swiperefresh: SwipeRefreshLayout
    private val leftCancel: Button
    private val title: QMUIQQFaceView
    var tipDialog:QMUITipDialog? =null
    private val mDiffRecyclerViewSaveStateId = QMUIViewHelper.generateViewId()

    override val layoutId: Int
        get() = R.layout.home_controller_picture

    private val titleStr = "相册"

    init {
        title = topbar.setTitle(titleStr)
        title.setTextColor(Color.BLACK)
        title.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
        leftCancel = topbar.addRightTextButton("取消 ", R.id.topbar_right_add_button)
        leftCancel.setTextColor(Color.BLACK)
        leftCancel.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
        leftCancel.visibility = View.GONE
        leftCancel.setOnClickListener {
            showPictureControl(false)
        }
        gridLayoutManager = MGridLayoutManager(context, 3)
        recyclerView = findViewById(R.id.recyclerView)
        swiperefresh = findViewById(R.id.swiperefresh)
        //设置进度View样式的大小，只有两个值DEFAULT和LARGE，表示默认和较大
        swiperefresh.setSize(SwipeRefreshLayout.DEFAULT);
        //设置触发下拉刷新的距离
        swiperefresh.setDistanceToTriggerSync(300);
        var scantip: TextView = findViewById(R.id.scantip)
        with(recyclerView) {
            layoutManager = gridLayoutManager
            adapter = mAdapter
        }
        swiperefresh?.setOnRefreshListener(this)
        mHomeViewModel.titleShow.observe(mHomeFragment, Observer {
            title.text = it
        })
        mHomeViewModel.folderData.observe(mhomeFragment, Observer {
            LogUtils.d(" mHomeViewModel. updateHomeMedialist")
            scantip.visibility = View.GONE
            swiperefresh?.isRefreshing = false
            tipDialog?.dismiss()
            if (it.isNullOrEmpty()) {
                var item_empty: View = View.inflate(context, R.layout.item_empty, null)
                var empty_tip = item_empty.findViewById<TextView>(R.id.empty_tip)
                var image = item_empty.findViewById<ImageView>(R.id.image)
                image.setImageResource(R.mipmap.share_ic_blank_album)
                empty_tip.setText("本地相册为空,快去拍照吧！")
                mAdapter.setEmptyView(item_empty)
            }
            mAdapter.setNewInstance(it)
            mHomeViewModel.count=it.size
        })
        mHomeViewModel.defUi.msgEvent.observe(mhomeFragment, Observer {
            if (it.code == 100) {
                tipDialog = QMUITipDialog.Builder(context)
                        .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                        .setTipWord(it.msg)
                        .create()
                tipDialog?.show()
            } else {
                MToastUtils.show("已经删除")
                tipDialog?.dismiss()
            }
        })
        mHomeViewModel.folderData()
    }

    override fun dispatchSaveInstanceState(container: SparseArray<Parcelable?>?) {
        val id: Int = recyclerView.getId()
        recyclerView.setId(mDiffRecyclerViewSaveStateId)
        super.dispatchSaveInstanceState(container)
        recyclerView.setId(id)
    }

    override fun dispatchRestoreInstanceState(container: SparseArray<Parcelable?>?) {
        val id: Int = recyclerView.getId()
        recyclerView.setId(mDiffRecyclerViewSaveStateId)
        super.dispatchRestoreInstanceState(container)
        recyclerView.setId(id)
    }

    override fun showPictureControl(isShow: Boolean?) {
        isShow?.let {
            mHomeControlListener.showPictureControl(isShow)
            if (isShow) {
                leftCancel.visibility = View.VISIBLE
            } else {
                leftCancel.visibility = View.GONE
            }
            mAdapter.updateSelect(isShow)
        }

    }

    override fun changeSelectData(isinit: Boolean, isSelectModel: Boolean, isAdd: Boolean, item: MediaInfo?) {
        mHomeViewModel.changeSelectData(isinit,isSelectModel,isAdd,item)
    }

    override fun getHomeFragment(): HomeFragment {
        return mHomeFragment
    }

    override fun onBackPressedExit() {
        showPictureControl(false)
    }

    override fun onRefresh() {
        LogUtils.d(" PictureController .onRefresh")
        mHomeViewModel.folderData()
    }
}


open class SpanSizeLookup(var adapterV2: LocalAlbumAdapterV2) : GridLayoutManager.SpanSizeLookup() {
    override fun getSpanSize(position: Int): Int {

        val get = adapterV2.data[position]
        return when (get.itemType) {
            FileType.Album -> 1
            FileType.ImageViewItem -> 1
            FileType.TimeTitle -> 3
            else -> 1
        }

    }

}
