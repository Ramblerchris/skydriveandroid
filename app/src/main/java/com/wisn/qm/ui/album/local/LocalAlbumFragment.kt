package com.wisn.qm.ui.album.local

import android.graphics.Color
import android.graphics.Typeface
import android.view.View
import android.widget.Button
import androidx.recyclerview.widget.GridLayoutManager
import com.library.base.BaseFragment
import com.qmuiteam.qmui.qqface.QMUIQQFaceView
import com.wisn.qm.R
import com.wisn.qm.mode.db.beans.Folder
import com.wisn.qm.mode.db.beans.MediaInfo
import com.wisn.qm.ui.album.AlbumViewModel
import com.wisn.qm.ui.home.HomeFragment
import kotlinx.android.synthetic.main.fragment_localalbum.*
import kotlinx.android.synthetic.main.fragment_newalbum.recyclerView
import kotlinx.android.synthetic.main.fragment_newalbum.topbar
import kotlinx.android.synthetic.main.fragment_register.*


class LocalAlbumFragment(var folder: Folder) : BaseFragment<AlbumViewModel>(), LocalCallBack {
    lateinit var title: QMUIQQFaceView
    lateinit var leftCancel: Button
    val newAlbumAdapter by lazy { LoalAdapterV2(this) }

    override fun layoutId(): Int {
        return R.layout.fragment_localalbum
    }

    override fun initView(views: View) {
        super.initView(views)
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
    }


    override fun showPictureControl(isShow: Boolean?) {
    }

    override fun changeSelectData(
        isinit: Boolean,
        isSelectModel: Boolean,
        isAdd: Boolean,
        item: MediaInfo?
    ) {
     }

    override fun getHomeFragment(): HomeFragment {
        return HomeFragment();
    }

}