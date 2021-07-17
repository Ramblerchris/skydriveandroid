package com.wisn.qm.ui.album.details

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import com.wisn.qm.R
import kotlinx.android.synthetic.main.item_loadstate.view.*

/**
 *
 * @Description:
 * @Author: Wisn
 * @CreateDate: 2020/12/22 上午11:40
 */
@Deprecated("")
class LoadStateViewHolder (parent: ViewGroup, var retry: () -> Unit) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context)
                .inflate(R.layout.item_loadstate, parent, false)
) {

    fun bindState(loadState: LoadState) {
        when (loadState) {
            is LoadState.Error -> {
                if(loadState.error is LoadDataError){
                    itemView.btn_retry.visibility = View.VISIBLE
                    itemView.ll_loading.visibility = View.GONE
                    itemView.btn_retry.text="重试"
                    itemView.btn_retry.setOnClickListener {
                        retry()
                    }
                    Log.d("LoadStateViewHolder", "Error")
                }
                if(loadState.error is NoMoreDataError){
                    Log.d("LoadStateViewHolder", "--"+loadState)
                    itemView.btn_retry.visibility=View.VISIBLE
                    itemView.ll_loading.visibility = View.GONE
                    itemView.btn_retry.text="没有更多了"
                }
            }
            is LoadState.Loading -> {
                Log.d("LoadStateViewHolder", "Loading")
                itemView.ll_loading.visibility = View.VISIBLE
                itemView.btn_retry.visibility=View.GONE
            }
            else -> {
                Log.d("LoadStateViewHolder", "--"+loadState)
                itemView.btn_retry.visibility=View.VISIBLE
                itemView.ll_loading.visibility = View.GONE
                itemView.btn_retry.text="没有更多了"
            }
        }

    }
}