package com.library.base.base

/**
 *
 * @Description:
 * @Author: Wisn
 * @CreateDate: 2021/2/1 下午3:31
 */
sealed class LoadState(val msg: String = "", val exdate: Any? = null) {
//    class Loading(msg: String) : LoadState(msg)
//    class Success(msg: String) : LoadState(msg)
//    class Fail(msg: String) : LoadState(msg)
//    class Refresh(msg: String) : LoadState(msg)
}