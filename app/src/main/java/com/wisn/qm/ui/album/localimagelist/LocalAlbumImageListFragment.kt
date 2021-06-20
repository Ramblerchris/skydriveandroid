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
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_localalbum.*
import kotlinx.android.synthetic.main.fragment_newalbum.recyclerView
import kotlinx.android.synthetic.main.fragment_newalbum.topbar
import kotlinx.android.synthetic.main.item_photo_select_bottom.*
import kotlinx.android.synthetic.main.item_photo_select_bottom.view.*


class LocalAlbumImageListFragment(var folder: Folder) : BaseFragment<AlbumViewModel>(), LocalCallBack {
    lateinit var title: QMUIQQFaceView
    lateinit var leftCancel: Button
    val TAG: String = "LocalAlbumImageListFragment"

    val newAlbumAdapter by lazy { LoalAlbumImageListAdapterV2(this) }

    override fun layoutId(): Int {
        return R.layout.fragment_localalbum
    }

    override fun initView(views: View) {
        super.initView(views)
        showPictureControl(false);
        title = topbar?.setTitle(folder.name)!!
        title.setTextColor(Color.BLACK)
        title.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
       /* leftCancel = topbar?.addLeftTextButton("取消", R.id.topbar_right_add_button)!!
        leftCancel.setTextColor(Color.BLACK)
        leftCancel.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
        leftCancel.visibility = View.VISIBLE
        leftCancel.setOnClickListener {
            popBackStack()
        }*/
        val addLeftBackImageButton = topbar?.addLeftBackImageButton();
        addLeftBackImageButton?.setColorFilter(Color.BLACK)
        addLeftBackImageButton?.setOnClickListener {
            popBackStack()
        }
        swiperefresh?.isEnabled=false
       /* var right = topbar?.addRightTextButton("添加", R.id.topbar_right_add_button)!!
        right.setTextColor(Color.BLACK)
        right.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
        right.setOnClickListener {
            val text = et_albumName?.text?.trim()
            if (text.isNullOrEmpty()) {
                MToastUtils.show("请输入相册名称")
            } else {
                text.run {
//                    hideSoftInput(et_albumName?.windowToken,false)
                    KeyboardUtils.hideSoftInput(et_albumName)
                    viewModel.addUserDir(text.toString()).observe(this@LocalAlbumFragment, Observer {
//                    MToastUtils.show(it.toString())
                       *//* var ait=it
                        val selectDate = newAlbumAdapter.getSelectDate()
                        selectDate?.let {
                            viewModel.saveMedianInfo( it as ArrayList<MediaInfo>,ait)
//                            viewModel.saveMedianInfo( it as ArrayList<MediaInfo>,)

                        }
                        LiveEventBus
                                .get(ConstantKey.updateAlbum)
                                .post(1);
                        popBackStack()*//*
                    })
                }
            }
        }*/
        var gridLayoutManager = GridLayoutManager(context, 3)
        with(recyclerView!!) {
            adapter = newAlbumAdapter
            layoutManager = gridLayoutManager
        }
        newAlbumAdapter.setNewData(folder.images)

        initEditView()
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
    //                    viewModel.deleteSelect()
                    //todo 添加loading dialog
                }
                .create(R.style.QMUI_Dialog).show()
    //            viewModel.saveMedianInfo(0)
    //            pictureController?.onBackPressedExit()
    //            MToastUtils.show("已经添加到上传任务")
        }
        item_photo_select_bottom.tv_upload.onClick {
    //            viewModel.saveMedianInfo(0)
    //            pictureController?.onBackPressedExit()
            MToastUtils.show("已经添加到上传任务")
        }
        item_photo_select_bottom.tv_addto.onClick {
            val values = folder.images;
            values?.let {
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
    //                        viewModel.saveMedianInfo(position)
    //                        MToastUtils.show("已经添加到上传任务")
    //                        pictureController?.onBackPressedExit();
                    }
                for (dirlist in values) {
    //                    builder.addItem(ContextCompat.getDrawable(context!!, R.mipmap.icon_tabbar_lab), "Item $i")
                    builder.addItem(dirlist.fileName)
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

    fun Exit():Boolean {
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
            item_photo_select_bottom?.visibility = if (isShow) View.VISIBLE else View.GONE
            newAlbumAdapter.updateSelect(false)
        }
    }

    override fun changeSelectData(
        isinit: Boolean,
        isSelectModel: Boolean,
        isAdd: Boolean,
        item: MediaInfo?
    ) {

     }

    override fun getQMUIFragment(): QMUIFragment {
       return this
    }


}