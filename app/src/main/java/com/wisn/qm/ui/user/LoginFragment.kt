package com.wisn.qm.ui.user

import android.graphics.Color
import android.graphics.Typeface
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.lifecycle.Observer
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ToastUtils
import com.library.base.BaseFragment
import com.library.base.utils.MToastUtils
import com.wisn.qm.R
import com.wisn.qm.ui.home.HomeFragment
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_login.topbar

/**
 * Created by Wisn on 2020/6/6 下午5:06.
 */
class LoginFragment : BaseFragment<UserViewModel>() {
    private val TAG: String = "LoginFragment"
    override fun initView(views: View) {
        super.initView(views)
        initTopBar()
//        viewModel.getResult()?.observe(this, Observer { s -> Toast.makeText(getContext(), s, Toast.LENGTH_LONG).show() })
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
            if(actionId== EditorInfo.IME_ACTION_DONE){
                commitDate()
                return@OnEditorActionListener true
            }
            return@OnEditorActionListener false
        })
    }

    private fun commitDate() {
        Log.d(TAG, "测试11" + et_phone?.text.toString())
        var phone = et_phone?.text.toString();
        var password = et_password?.text.toString()
        if (phone.isNullOrEmpty()) {
            MToastUtils.show("请输入手机号")
            return;
        }
        if (password.isNullOrEmpty()) {
            MToastUtils.show("请输入密码")
            return;
        }
        //            hideSoftInput(et_password.windowToken,false)
        KeyboardUtils.hideSoftInput(et_password)
        viewModel.login(phone, password)
    }

    override fun onResume() {
        super.onResume()
        et_phone?.requestFocus()
    }
    private fun initTopBar() {
//        val  addLeftBackImageButton =  topbar?.addLeftBackImageButton();
//        addLeftBackImageButton?.setColorFilter(Color.BLACK);
//        addLeftBackImageButton?.setOnClickListener { popBackStack() }
        val title = topbar?.setTitle("登录");
        title?.setTextColor(Color.BLACK)
        title?.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))

    }

    override fun layoutId(): Int {
        return R.layout.fragment_login;
    }
}