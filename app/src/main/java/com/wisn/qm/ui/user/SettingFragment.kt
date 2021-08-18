package com.wisn.qm.ui.user

import android.graphics.Color
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import com.blankj.utilcode.util.AppUtils
import com.library.base.BaseFragment
import com.library.base.config.GlobalConfig
import com.library.base.utils.MToastUtils
import com.qmuiteam.qmui.qqface.QMUIQQFaceView
import com.qmuiteam.qmui.skin.QMUISkinManager
import com.qmuiteam.qmui.util.QMUIDisplayHelper
import com.qmuiteam.qmui.widget.dialog.QMUIDialog.MessageDialogBuilder
import com.qmuiteam.qmui.widget.grouplist.QMUICommonListItemView
import com.qmuiteam.qmui.widget.grouplist.QMUIGroupListView
import com.wisn.qm.R
import kotlinx.android.synthetic.main.fragment_setting.*


open class SettingFragment : BaseFragment<UserViewModel>(), View.OnClickListener {
    lateinit var title: QMUIQQFaceView
    val tipRing by lazy {
        groupListView?.createItemView(
            null,
            "上传提示音",
            null,
            QMUICommonListItemView.HORIZONTAL,
            QMUICommonListItemView.ACCESSORY_TYPE_SWITCH
        )
    }
    val tipVibrate by lazy {
        groupListView?.createItemView(
            null,
            "上传提示震动",
            null,
            QMUICommonListItemView.HORIZONTAL,
            QMUICommonListItemView.ACCESSORY_TYPE_SWITCH
        )
    }
    val autoUpload by lazy {
        groupListView?.createItemView(
            null,
            "自动同步",
            null,
            QMUICommonListItemView.HORIZONTAL,
            QMUICommonListItemView.ACCESSORY_TYPE_SWITCH
        )
    }
    val lowBatteryUpload by lazy {
        groupListView?.createItemView(
            null,
            "低电量同步",
            null,
            QMUICommonListItemView.HORIZONTAL,
            QMUICommonListItemView.ACCESSORY_TYPE_SWITCH
        )
    }
    val previewImageOrigin by lazy {
        groupListView?.createItemView(
            null,
            "自动加载原图",
            null,
            QMUICommonListItemView.HORIZONTAL,
            QMUICommonListItemView.ACCESSORY_TYPE_SWITCH
        )
    }
    val serverManager by lazy {
        groupListView?.createItemView(
            null,
            "服务器管理",
            " ",
            QMUICommonListItemView.HORIZONTAL,
            QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON
        )
    }
    val versionItem by lazy {
        groupListView?.createItemView(
            null,
            "版本号",
            AppUtils.getAppVersionName(),
            QMUICommonListItemView.HORIZONTAL,
            QMUICommonListItemView.ACCESSORY_TYPE_NONE
        )
    }
    val about by lazy {
        groupListView?.createItemView(
            null,
            "关于APP",
            " ",
            QMUICommonListItemView.HORIZONTAL,
            QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON
        )
    }

    override fun layoutId() = R.layout.fragment_setting
    override fun initView(views: View) {
        super.initView(views)
        title = topbar?.setTitle("设置")!!
        title.setTextColor(Color.BLACK)
        title.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
        val addLeftBackImageButton = topbar?.addLeftBackImageButton()
        addLeftBackImageButton?.setColorFilter(Color.BLACK)
        addLeftBackImageButton?.setOnClickListener {
            popBackStack()
        }
        tipRing?.switch?.setOnCheckedChangeListener { _: CompoundButton, b: Boolean ->
            if (b) {
                tipRing?.setDetailText("开")
            } else {
                tipRing?.setDetailText("关")
            }
            GlobalConfig.saveTipRing(b)
        }

        tipVibrate?.switch?.setOnCheckedChangeListener { _: CompoundButton, b: Boolean ->
            if (b) {
                tipVibrate?.setDetailText("开")
            } else {
                tipVibrate?.setDetailText("关")
            }
            GlobalConfig.saveTipVibrate(b)
        }
        autoUpload?.switch?.setOnCheckedChangeListener { _: CompoundButton, b: Boolean ->
            if (b) {
                autoUpload?.setDetailText("自动")
            } else {
                autoUpload?.setDetailText("手动")
            }
        }
        lowBatteryUpload?.switch?.setOnCheckedChangeListener { _: CompoundButton, b: Boolean ->
            if (b) {
                lowBatteryUpload?.setDetailText("是")
            } else {
                lowBatteryUpload?.setDetailText("否")
            }
            GlobalConfig.saveLowBatteryUpload(b)
        }
        previewImageOrigin?.switch?.setOnCheckedChangeListener { _: CompoundButton, b: Boolean ->
            if (b) {
                previewImageOrigin?.setDetailText("是")
            } else {
                previewImageOrigin?.setDetailText("否")
            }
            GlobalConfig.previewImageOrigin(b)
        }

        QMUIGroupListView.newSection(context)
            .setTitle("")
            .setDescription("")
            .setLeftIconSize(
                QMUIDisplayHelper.dp2px(context, 18),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            .addItemView(tipRing, this)
            .addItemView(tipVibrate, this)
//                .addItemView(autoUpload, this)
            .addItemView(lowBatteryUpload, this)
            .addItemView(previewImageOrigin, this)
            .addItemView(serverManager, this)
            .addItemView(versionItem, this)
            .addItemView(about, this)
            .setMiddleSeparatorInset(QMUIDisplayHelper.dp2px(context, 18), 0)
            .addTo(groupListView)
        tipRing?.switch?.isChecked = GlobalConfig.tipRing
        tipVibrate?.switch?.isChecked = GlobalConfig.tipVibrate
        lowBatteryUpload?.switch?.isChecked = GlobalConfig.lowBatteryUpload
        previewImageOrigin?.switch?.isChecked = GlobalConfig.previewImageOrigin
    }

    override fun onClick(v: View?) {
        if (v == versionItem) {
            MToastUtils.show("当前为最新版本")
        } else if (v == about) {
            MessageDialogBuilder(activity)
                .setTitle("关于APP")
                .setSkinManager(QMUISkinManager.defaultInstance(context))
                .setMessage("局域网媒体文件同步的app")
                .addAction("取消") { dialog, _ ->
                    dialog.dismiss()
                }
                .create(R.style.QMUI_Dialog).show()
        } else if (v == serverManager) {
            startFragment(ShutdownServerFragment())
        }

    }

}