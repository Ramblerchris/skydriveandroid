package com.wisn.qm.ui.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.chad.library.adapter.base.loadmore.BaseLoadMoreView
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.wisn.qm.R

class LoadMoreAndFooterView : BaseLoadMoreView() {

    override fun getLoadComplete(baseViewHolder: BaseViewHolder): View {
        return baseViewHolder.getView(R.id.ll_loadcomplete)
    }

    override fun getLoadEndView(baseViewHolder: BaseViewHolder): View {
        return baseViewHolder.getView(R.id.ll_loadfinish)

    }

    override fun getLoadFailView(baseViewHolder: BaseViewHolder): View {
        return baseViewHolder.getView(R.id.ll_fail)

    }

    override fun getLoadingView(baseViewHolder: BaseViewHolder): View {
        return baseViewHolder.getView(R.id.ll_loadmore)

    }

    override fun getRootView(viewGroup: ViewGroup): View {
        return LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_rv_loadmore_footer, viewGroup, false)
    }
}