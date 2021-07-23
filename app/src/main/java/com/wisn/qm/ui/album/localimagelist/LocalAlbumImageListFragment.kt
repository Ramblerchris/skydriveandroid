package com.wisn.qm.ui.album.localimagelist

import android.graphics.Color
import android.graphics.Typeface
import android.view.View
import android.widget.Button
import androidx.recyclerview.widget.GridLayoutManager
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.VibrateUtils
import com.library.base.BaseFragment
import com.library.base.utils.MToastUtils
import com.qmuiteam.qmui.alpha.QMUIAlphaImageButton
import com.qmuiteam.qmui.arch.QMUIFragment
import com.qmuiteam.qmui.kotlin.onClick
import com.qmuiteam.qmui.qqface.QMUIQQFaceView
import com.qmuiteam.qmui.skin.QMUISkinManager
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import com.wisn.qm.R
import com.wisn.qm.mode.db.beans.Folder
import com.wisn.qm.mode.db.beans.MediaInfo
import com.wisn.qm.ui.album.AlbumViewModel
import com.wisn.qm.ui.album.newalbum.NewAlbumFragment
import kotlinx.android.synthetic.main.fragment_localalbum.*
import kotlinx.android.synthetic.main.fragment_newalbum.recyclerView
import kotlinx.android.synthetic.main.fragment_newalbum.topbar
import kotlinx.android.synthetic.main.item_photo_select_bottom.view.*

class LocalAlbumImageListFragment(var folder: Folder) : BaseFragment<AlbumViewModel>(),
    LocalCallBack {
    lateinit var title: QMUIQQFaceView
    lateinit var    leftCancel:Button
    lateinit var    addLeftBackImageButton: QMUIAlphaImageButton
    lateinit var    selectAllButtom: Button
    val TAG: String = "LocalAlbumImageListFragment"

    val newAlbumAdapter by lazy { LoalAlbumImageListAdapterV2(this) }

    override fun layoutId(): Int {
        return R.layout.fragment_localalbum
    }

    override fun initView(views: View) {
        super.initView(views)
        viewModel.getUserDirlist()
        title = topbar?.setTitle(folder.name)!!
        title.setTextColor(Color.BLACK)
        title.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
        leftCancel = topbar?.addLeftTextButton("取消", R.id.topbar_left_add_button)!!
        leftCancel.setTextColor(Color.BLACK)
        leftCancel.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
        leftCancel.visibility = View.GONE
        leftCancel.setOnClickListener {
            popBackStack()
        }
        addLeftBackImageButton = topbar?.addLeftBackImageButton()!!
        addLeftBackImageButton.setColorFilter(Color.BLACK)
        addLeftBackImageButton.setOnClickListener {
            popBackStack()
        }
        selectAllButtom = topbar?.addRightTextButton("全选", R.id.topbar_right_add_button)!!
        selectAllButtom.visibility=View.GONE
        selectAllButtom.setOnClickListener {
            val updateSelectAll = newAlbumAdapter.updateSelectAll()
            updateSelectAllText(updateSelectAll)
        }
        viewModel.titleStr = folder.name
        viewModel.titleShow.observe(this, {
            title.text = it
        })
        showPictureControl(false)
        swiperefresh?.isEnabled = false
        var gridLayoutManager = GridLayoutManager(context, 3)
        with(recyclerView!!) {
            adapter = newAlbumAdapter
            layoutManager = gridLayoutManager
        }
        viewModel.count = folder.images.size
        newAlbumAdapter.setNewData(folder.images)
        initEditView()
    }

    private fun updateSelectAllText(updateSelectAll: Boolean) {
        if (updateSelectAll) {
            selectAllButtom.text = "取消全选"
        } else {
            selectAllButtom.text = "全选"
        }
    }

    private fun initEditView() {
        item_photo_select_bottom.tv_delete.onClick {
            VibrateUtils.vibrate(30)
            QMUIDialog.MessageDialogBuilder(context)
                .setTitle("删除本地文件")
                .setSkinManager(QMUISkinManager.defaultInstance(context))
                .setMessage("确定要删除这些本地文件吗?")
                .addAction("取消") { dialog, _ ->
                    dialog.dismiss()
                }
                .addAction("确定") { dialog, _ ->
                    dialog.dismiss()
                    newAlbumAdapter.deleteSelect()
                    viewModel.deleteSelect()
                    Exit()
                }
                .create(R.style.QMUI_Dialog).show()
        }
        item_photo_select_bottom.tv_upload.onClick {
            viewModel.saveMedianInfo(0)
            newAlbumAdapter.resetSelect()
            MToastUtils.show("已经添加到上传任务")
            Exit()
        }
        item_photo_select_bottom.tv_addto.onClick {
            val value = viewModel.dirLevel1listLD.value
            if (value != null && value.size > 0) {
                val builder = QMUIBottomSheet.BottomListSheetBuilder(activity)
                var addItem = View.inflate(context, R.layout.item_album_new_album, null)
                builder.setGravityCenter(true)
                    .setSkinManager(QMUISkinManager.defaultInstance(context))
                    .setTitle("添加到")
                    .setAddCancelBtn(true)
                    .setAllowDrag(true)
                    .setNeedRightMark(true)
                    .setOnSheetItemClickListener { dialog, itemView, position, tag ->
                        dialog.dismiss()
                        viewModel.saveMedianInfo(position)
                        newAlbumAdapter.resetSelect()

                        MToastUtils.show("已经添加到上传任务")
                        Exit()
                    }
                for (dirlist in value) {
                    //                    builder.addItem(ContextCompat.getDrawable(context!!, R.mipmap.icon_tabbar_lab), "Item $i")
                    builder.addItem(dirlist.filename)
                }
                builder.addContentFooterView(addItem)
                val build = builder.build();
                build.show()
                addItem.onClick {
                    build.dismiss()
                    startFragment(NewAlbumFragment())
                }
            }


        }
    }

    override fun onBackPressed() {
        LogUtils.d(TAG, " HomeFragment.onBackPressed")
        if (Exit()) {
            super.onBackPressed()
        }
    }

    fun Exit(): Boolean {
        if (item_photo_select_bottom?.visibility == View.VISIBLE) {
            showPictureControl(false)
            return false
        }
        return true
    }

    override fun showPictureControl(isShow: Boolean?) {
        isShow?.let {
            if (isShow) {
                VibrateUtils.vibrate(10)
            }
            selectAllButtom.visibility = if (isShow) View.VISIBLE else View.GONE
            leftCancel.visibility = if (isShow) View.VISIBLE else View.GONE
            addLeftBackImageButton.visibility = if (isShow) View.GONE else View.VISIBLE
            item_photo_select_bottom?.visibility = if (isShow) View.VISIBLE else View.GONE
            newAlbumAdapter.updateSelect(false)
        }
    }

    override fun changeSelectData(
        isinit: Boolean,
        isSelectModel: Boolean,
        isSelectAll: Boolean,
        isAdd: Boolean,
        selectList: MutableList<MediaInfo>?
    ) {
        updateSelectAllText(isSelectAll)
        viewModel.changeSelectData(isinit, isSelectModel,isSelectAll, isAdd, selectList)

    }


    override fun getQMUIFragment(): QMUIFragment {
        return this
    }


}