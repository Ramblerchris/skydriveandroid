package com.wisn.qm.ui

import android.content.Intent
import android.os.Bundle
import com.library.base.BaseActivity
import com.library.base.base.NoViewModel
import com.library.base.config.GlobalUser
import com.library.base.utils.NetCheckUtils
import com.qmuiteam.qmui.arch.QMUIFragmentActivity
import com.qmuiteam.qmui.kotlin.onClick
import com.qmuiteam.qmui.util.QMUIStatusBarHelper
import com.wisn.qm.R
import com.wisn.qm.ui.check.NetCheckFragment
import com.wisn.qm.ui.user.LoginFragment
import kotlinx.android.synthetic.main.activity_splash1.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SplashActivity : BaseActivity<NoViewModel>() {
    override fun layoutId(): Int {
        return R.layout.activity_splash1
    }


    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
//        binding = ActivityMainBinding.inflate(getLayoutInflater());
        QMUIStatusBarHelper.setStatusBarLightMode(this)

        startmain?.onClick {
            startActivity(Intent(this, MainActivity::class.java))
        }
        GlobalScope.launch {
            val connectCheckInit = NetCheckUtils.isConnectCheckInit();
            if (connectCheckInit) {
                if (GlobalUser.token.isNullOrEmpty()) {
                    startActivity(
                        QMUIFragmentActivity.intentOf(
                            this@SplashActivity,
                            MainActivity::class.java,
                            LoginFragment::class.java
                        )
                    )
                } else {
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                }
            } else {
                startActivity(
                    QMUIFragmentActivity.intentOf(
                        this@SplashActivity,
                        MainActivity::class.java,
                        NetCheckFragment::class.java
                    )
                )
            }
            this@SplashActivity.finish()
        }
    }

    override fun overridePendingTransition(enterAnim: Int, exitAnim: Int) {
        super.overridePendingTransition(0, 0)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0);

    }

}