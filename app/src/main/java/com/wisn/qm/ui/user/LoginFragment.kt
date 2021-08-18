package com.wisn.qm.ui.user

import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import com.blankj.utilcode.util.KeyboardUtils
import com.library.base.BaseFragment
import com.library.base.config.SpConstant
import com.library.base.utils.KV
import com.library.base.utils.MToastUtils
import com.qmuiteam.qmui.kotlin.onClick
import com.wisn.qm.R
import com.wisn.qm.ui.home.HomeFragment
import kotlinx.android.synthetic.main.fragment_login.*

/**
 * Created by Wisn on 2020/6/6 下午5:06.
 */
class LoginFragment : BaseFragment<UserViewModel>() {
    private val TAG: String = "LoginFragment"
    override fun initView(views: View) {
        super.initView(views)
        login?.setOnClickListener {
            commitDate()
        }
        tv_register?.setOnClickListener {
            startFragment(RegisterFragment())
        }
        viewModel.defUi.msgEvent.observe(this, Observer {
            if (it.code == 100) {
                startFragmentAndDestroyCurrent(HomeFragment(), false)
            } else {
//                et_phone.requestFocus()
//                hideSoftInput(et_password.windowToken,true)
                KeyboardUtils.showSoftInput(et_phone)
            }
        })
        et_phone?.postDelayed({
            KeyboardUtils.showSoftInput(et_phone)
        }, 300)
        et_password.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                commitDate()
                return@OnEditorActionListener true
            }
            return@OnEditorActionListener false
        })
        et_password.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                val toString = et_password.text.toString()
                if (toString.isNotEmpty()) {
                    showpassword.visibility = View.VISIBLE
                } else {
                    showpassword.visibility = View.GONE
                }
            }
        })
        showpassword.onClick {
            isShowPassword = !isShowPassword
            if (isShowPassword) { //显示密码
                showpassword.setImageResource(R.mipmap.btn_ps_show)
                et_password.setTransformationMethod(HideReturnsTransformationMethod.getInstance())
                et_password.setSelection(et_password.text.length)
            } else { //隐藏密码
                showpassword.setImageResource(R.mipmap.btn_ps_hide)
                et_password.setTransformationMethod(PasswordTransformationMethod.getInstance())
                et_password.setSelection(et_password.text.length)
            }
        }
        et_phone?.setText( KV.getStr(SpConstant.Username))
//        et_password?.setText( KV.getStr(SpConstant.Password))
        et_password?.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
    }
    private var isShowPassword = false

    private fun commitDate() {
        Log.d(TAG, "测试11" + et_phone?.text.toString())
        var phone = et_phone?.text.toString();
        var password = et_password?.text.toString()
        if (phone.isNullOrEmpty()) {
            MToastUtils.show("请输入手机号")
            return
        }
        if (password.isNullOrEmpty()) {
            MToastUtils.show("请输入密码")
            return
        }
        //            hideSoftInput(et_password.windowToken,false)
        KeyboardUtils.hideSoftInput(et_password)
        viewModel.login(phone, password)

    }

    override fun onResume() {
        super.onResume()
        et_phone?.requestFocus()
    }

    override fun layoutId(): Int {
        return R.layout.fragment_login;
    }
}