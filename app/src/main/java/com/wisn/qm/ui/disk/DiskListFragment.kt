package com.wisn.qm.ui.disk

import com.library.base.BaseFragment
import com.wisn.qm.R
import com.wisn.qm.ui.check.NetCheckViewModel

/**
 *
 * @Description:
 * @Author: Wisn
 * @CreateDate: 2021/1/15 下午7:43
 */
class DiskListFragment: BaseFragment<NetCheckViewModel>() {
    override fun layoutId(): Int {
        return R.layout.fragment_disklist
    }
}