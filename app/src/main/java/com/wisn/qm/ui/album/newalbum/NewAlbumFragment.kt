package com.wisn.qm.ui.album.newalbum

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.jeremyliao.liveeventbus.LiveEventBus
import com.library.base.BaseFragment
import com.qmuiteam.qmui.qqface.QMUIQQFaceView
import com.wisn.qm.R
import com.wisn.qm.mode.ConstantKey
import com.wisn.qm.mode.db.beans.MediaInfo
import com.wisn.qm.ui.album.AlbumViewModel
import com.wisn.qm.ui.select.selectmedia.SelectMediaFragment
import kotlinx.android.synthetic.main.fragment_newalbum.*
import kotlinx.android.synthetic.main.fragment_newalbum.recyclerView
import kotlinx.android.synthetic.main.fragment_newalbum.topbar


class NewAlbumFragment : BaseFragment<AlbumViewModel>(), ClickItem {
    lateinit var title: QMUIQQFaceView
    lateinit var leftCancel: Button
    val newAlbumAdapter by lazy { NewAlbumAdapter1(this) }

    override fun layoutId(): Int {
        return R.layout.fragment_newalbum
    }

    override fun initView(views: View) {
        super.initView(views)
        title = topbar?.setTitle("新建相册")!!
        title.setTextColor(Color.BLACK)
        title.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
        leftCancel = topbar?.addLeftTextButton("取消", R.id.topbar_right_add_button)!!
        leftCancel.setTextColor(Color.BLACK)
        leftCancel.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
        leftCancel.visibility = View.VISIBLE
        leftCancel.setOnClickListener {
            popBackStack()
        }
        var right = topbar?.addRightTextButton("添加", R.id.topbar_right_add_button)!!
        right.setTextColor(Color.BLACK)
        right.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
        right.setOnClickListener {
            val text = et_albumName?.text?.trim()
            if (text.isNullOrEmpty()) {
                ToastUtils.showShort("请输入相册名称")
            } else {
                text.run {
                    hideSoftInput(et_albumName?.windowToken,false)
                    viewModel.addUserDir(text.toString()).observe(this@NewAlbumFragment, Observer {
//                    ToastUtils.showShort(it.toString())
                        var ait=it
                        val selectDate = newAlbumAdapter.getSelectDate()
                        selectDate?.let {
                            viewModel.saveMedianInfo( it as ArrayList<MediaInfo>,ait)
//                            viewModel.saveMedianInfo( it as ArrayList<MediaInfo>,)

                        }
                        LiveEventBus
                                .get(ConstantKey.updateAlbum)
                                .post(1);
                        popBackStack()
                    })
                }
            }
        }
        var gridLayoutManager = GridLayoutManager(context, 3)
        with(recyclerView!!) {
            adapter = newAlbumAdapter
            layoutManager = gridLayoutManager
        }
        newAlbumAdapter.setNewData(null)
        et_albumName?.post {   hideSoftInput(et_albumName?.windowToken,true)  }
    }

    override fun onFragmentResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onFragmentResult(requestCode, resultCode, data)
        var list = data?.extras?.getSerializable("data") as ArrayList<MediaInfo>
        LogUtils.d("onFragmentResult", list)
        newAlbumAdapter.setNewData(list);
    }


    override fun click(isadd: Boolean, position: Int, fileBean: MediaInfo) {
        if (isadd) {
            hideSoftInput(et_albumName.windowToken,false)
            val selectPictureFragment = SelectMediaFragment()
            selectPictureFragment.arguments = Bundle()
            selectPictureFragment.selectList = newAlbumAdapter.getSelectDate()
            startFragmentForResult(selectPictureFragment, 100)
        }
    }

}