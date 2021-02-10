package com.wisn.qm.ui.user

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.blankj.utilcode.util.LogUtils
import com.library.base.BaseFragment
import com.library.base.config.Constant
import com.qmuiteam.qmui.kotlin.onClick
import com.qmuiteam.qmui.qqface.QMUIQQFaceView
import com.qmuiteam.qmui.skin.QMUISkinManager
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import com.wisn.qm.R
import kotlinx.android.synthetic.main.fragment_netcheck.*

import kotlinx.android.synthetic.main.fragment_server.*
import kotlinx.android.synthetic.main.fragment_server.result
import kotlinx.android.synthetic.main.fragment_server.rg_model
import kotlinx.android.synthetic.main.fragment_server.select_ok
import kotlinx.android.synthetic.main.fragment_server.spinner1
import kotlinx.android.synthetic.main.fragment_server.spinner2
import kotlinx.android.synthetic.main.fragment_server.topbar
import kotlinx.android.synthetic.main.fragment_server.tv_info

/**
 * Created by Wisn on 2020/6/6 下午5:08.
 */
class ShutdownServerFragment : BaseFragment<UserViewModel>() {
    lateinit var title: QMUIQQFaceView
    var ipAddressByWifi: String? = null
    override fun layoutId(): Int {
        return R.layout.fragment_server
    }

    private fun initTopBar() {
        title = topbar?.setTitle("服务器管理")!!
        title.setTextColor(Color.BLACK)
        title.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
        val addLeftBackImageButton = topbar?.addLeftBackImageButton()
        addLeftBackImageButton?.setColorFilter(Color.BLACK)
        addLeftBackImageButton?.setOnClickListener {
            popBackStack()
        }
    }


    override fun initView(views: View) {
        super.initView(views)
        initTopBar()
        select_ok?.onClick {
            LogUtils.d("getNetinfo 222", Thread.currentThread().name)
            var type = ""
            var typeMessage = ""
            var timeMessage = ""
            if (rg_model.checkedRadioButtonId == R.id.radioButton_shutdown) {
                typeMessage = "服务器关机"
                type = "sd_off"
            } else {
                typeMessage = "服务器重启"
                type = "sd_reboot"
            }
            var time = "立刻"
            if (cb_time.isChecked) {
                val sp1 = spinner1?.selectedItem.toString()
                val sp2 = spinner2?.selectedItem.toString()
                time = "$sp1:$sp2"
                timeMessage = "定时(${time})"
            }
            QMUIDialog.MessageDialogBuilder(context)
                    .setTitle(typeMessage)
                    .setSkinManager(QMUISkinManager.defaultInstance(context))
                    .setMessage("确定要${timeMessage} ${typeMessage}?")
                    .addAction("取消") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .addAction("确定") { dialog, _ ->
                        dialog.dismiss()
                        viewModel.adminSDRB(type, time ,"${timeMessage} ${typeMessage}")
                    }
                    .create(R.style.QMUI_Dialog).show()
        }
        select_cancel?.onClick {
            QMUIDialog.MessageDialogBuilder(context)
                    .setTitle("清除所有任务")
                    .setSkinManager(QMUISkinManager.defaultInstance(context))
                    .setMessage("确定要清除所有任务?")
                    .addAction("暂不清除") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .addAction("清除") { dialog, _ ->
                        dialog.dismiss()
                        viewModel.cancelAdminSDRB()
                    }
                    .create(R.style.QMUI_Dialog).show()
        }
        result?.setMovementMethod(ScrollingMovementMethod.getInstance());
        tv_info?.text = "服务器地址 IP:${Constant.BASE_URL}"
        try {
            val split = ipAddressByWifi?.split(".")
            setAdapter(requireContext(), spinner1!!, 1, null, split?.get(0))
            setAdapter(requireContext(), spinner2!!, 2, split?.get(0), split?.get(1))
            spinner1!!.setOnItemSelectedListener(object : OnItemSelectedListener {
                override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                    val info = adapterView.getItemAtPosition(i).toString() //获取i所在的文本
                    setAdapter(requireContext(), spinner2!!, 2, info, split?.get(1))
                }

                override fun onNothingSelected(adapterView: AdapterView<*>?) {

                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
        viewModel.defUi.msgEvent.observe(this, androidx.lifecycle.Observer {
            val text = result?.text;
            result?.text = "$text \n${it.msg}"
        })
    }

    fun setAdapter(context: Context, spanner: Spinner, position: Int, lastSelect: String?, selectStr: String?) {
        var spinnerItems: MutableList<String> = ArrayList<String>();
        if (position == 1) {
            for (index in 0 until 24) {
                spinnerItems.add(index.toString())
            }
        } else if (position == 2) {
            for (index in 0 until 60) {
                spinnerItems.add(index.toString())
            }
        }
        var spinnerAdapter = ArrayAdapter<String>(context, R.layout.item_spinner_textview, spinnerItems)
        spinnerAdapter.setDropDownViewResource(R.layout.item_spinner_textview)
        spanner.setAdapter(spinnerAdapter)
        selectStr?.let {
            val indexOf = spinnerItems.indexOf(selectStr)
            if (indexOf >= 0) {
                spanner.setSelection(indexOf)
            }
        }

    }
}