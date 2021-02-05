package com.wisn.qm.ui.select.selectmedia

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.view.View
import android.widget.Button
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.library.base.BaseFragment
import com.qmuiteam.qmui.qqface.QMUIQQFaceView
import com.wisn.qm.R
import com.wisn.qm.mode.beans.FileType
import com.wisn.qm.mode.db.beans.MediaInfo
import com.wisn.qm.ui.select.SelectFileViewModel
import kotlinx.android.synthetic.main.fragment_selectfile.*

open class SelectMediaFragment(var maxSelect:Int=-1) : BaseFragment<SelectFileViewModel>() {
    lateinit var title: QMUIQQFaceView
    lateinit var leftCancel: Button
    lateinit var rightButton: Button
    private val mAdapter by lazy { SelectMediaAdapter(viewModel,maxSelect) }
    lateinit var gridLayoutManager: GridLayoutManager
    var selectList: MutableList<MediaInfo>? = null
    var selectMediaCall:SelectMediaCall?=null
    override fun layoutId(): Int {
        return R.layout.fragment_selectmedia
    }

    override fun initView(views: View) {
        title = topbar?.setTitle("照片库")!!
        title.setTextColor(Color.BLACK)
        title.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
        leftCancel = topbar?.addLeftTextButton("取消", R.id.topbar_right_add_button)!!
        leftCancel.setTextColor(Color.BLACK)
        leftCancel.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
        leftCancel.visibility = View.VISIBLE
        leftCancel.setOnClickListener {
            popBackStack()
        }
        rightButton = topbar?.addRightTextButton("确定", R.id.topbar_right_add_button)!!
        rightButton.setTextColor(Color.BLACK)
        rightButton.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
        rightButton.setOnClickListener {
            var intent = Intent()
            val value = viewModel.selectData().value
            value?.let {
                selectMediaCall?.setResult(it)
                intent.putExtra("data", value)
                setFragmentResult(101, intent)
            }
            popBackStack()
        }
        gridLayoutManager = GridLayoutManager(context, 3)
        with(gridLayoutManager) {
            spanSizeLookup = SelectSpanSizeLookup(mAdapter)
        }
        with(recyclerView) {
            this?.layoutManager = gridLayoutManager
            this?.adapter = mAdapter
        }

        viewModel.getMediaImageList(selectList).observe(this, Observer {
            if(selectList==null){
                mAdapter.setNewInstance(it)
            }else{
                recyclerView.postDelayed({
                    mAdapter.setNewInstance(it)
                }, 100)
                viewModel.mediaSelectList.value?.size?.let { it1 -> setTitleTipInfo(it1) }
            }
        })

        viewModel.selectData().observe(this, Observer {
            setTitleTipInfo(it.size)
        })

    }

    private fun setTitleTipInfo(cout:Int) {
        if (cout> 0) {
            rightButton.visibility = View.VISIBLE
            title.text = "已选中${cout}项"
        } else {
            rightButton.visibility = View.GONE
            title.text = "请选择"
        }
    }

}

open class SelectSpanSizeLookup(var adapterV2: SelectMediaAdapter) : GridLayoutManager.SpanSizeLookup() {
    override fun getSpanSize(position: Int): Int {

        val get = adapterV2.data[position]
        return when (get.itemType) {
            FileType.ImageViewItem -> 1
            FileType.TimeTitle -> 3
            else -> 1
        }

    }

}
interface SelectMediaCall {
    fun setResult(result:ArrayList<MediaInfo>)
}