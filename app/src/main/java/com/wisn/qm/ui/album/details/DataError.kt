package com.wisn.qm.ui.album.details

/**
 *
 * @Description:
 * @Author: Wisn
 * @CreateDate: 2020/12/24 下午3:09
 */
//没有数据
@Deprecated("")
class EmptyDataError(override val cause: Throwable?): Throwable(cause=cause) {
}

//加载数据出错
@Deprecated("")
class LoadDataError (override val cause: Throwable?): Throwable(cause=cause) {
}

//没有更多数据了
@Deprecated("")
class NoMoreDataError (override val cause: Throwable?): Throwable(cause=cause) {
}
