package com.wisn.qm.ui.user

import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.lifecycle.Observer
import com.blankj.utilcode.util.KeyboardUtils
import com.library.base.BaseFragment
import com.library.base.config.SpConstant
import com.library.base.utils.KV
import com.library.base.utils.MToastUtils
import com.wisn.qm.R
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_register.*
import kotlinx.android.synthetic.main.fragment_register.et_password
import kotlinx.android.synthetic.main.fragment_register.et_phone
import kotlinx.android.synthetic.main.fragment_register.login
import kotlinx.android.synthetic.main.fragment_register.topbar

/**
 * Created by Wisn on 2020/6/6 下午5:06.
 */
class RegisterFragment : BaseFragment<UserViewModel>() {


    override fun initView(views: View) {
        super.initView(views)
        initTopBar()
        et_register?.setOnClickListener {
            commitData()
        }
        login?.setOnClickListener {
//            startFragment(LoginFragment())
            popBackStack()
        }
        viewModel.defUi.msgEvent.observe(this, Observer {
            if (it.code == 100) {
                popBackStack()
            }
        })
        et_phone?.post {
            KeyboardUtils.showSoftInput(et_phone)
        }
        et_password.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if(actionId== EditorInfo.IME_ACTION_DONE){
                commitData()
                return@OnEditorActionListener true
            }
            return@OnEditorActionListener false
        })
        et_phone?.setText( KV.getStr(SpConstant.Username))
    }

    private fun commitData() {
        var phone = et_phone?.text.toString();
        var password = et_password?.text.toString()
        var etPassword2 = et_password2?.text.toString()
        if (phone.isEmpty()) {
            MToastUtils.show("请输入手机号")
            return;
        }
        if (password.isEmpty()) {
            MToastUtils.show("请设置密码")
            return;
        }
        if (etPassword2.isEmpty()) {
            MToastUtils.show("请再次确认密码")
            return;
        }
        viewModel.register(phone, password, et_password.text.toString())
    }

    private fun initTopBar() {
        val addLeftBackImageButton = topbar?.addLeftBackImageButton();
//        addLeftBackImageButton?.setColorFilter(Color.BLACK)
        addLeftBackImageButton?.setOnClickListener { popBackStack() }

//        val title = topbar?.setTitle("注册");
//        title?.setTextColor(Color.BLACK)
//        title?.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))

    }

    override fun layoutId(): Int {
        return R.layout.fragment_register;
    }
}