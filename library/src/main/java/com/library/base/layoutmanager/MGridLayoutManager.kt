package com.library.base.layoutmanager

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 *
 * @Description:
 * @Author: Wisn
 * @CreateDate: 2021/2/15 上午11:41
 */
class MGridLayoutManager(context: Context?, spanCount: Int) : GridLayoutManager(context, spanCount) {
    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        try {
            super.onLayoutChildren(recycler, state)
        } catch (e: Exception) {
        }
    }
}