package com.wisn.qm.ui.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.library.base.utils.ImageAdapter
import com.wisn.qm.R
import com.wisn.qm.mode.db.beans.UserDirBean
import com.wisn.qm.ui.home.adapter.OnLineAlbumAdapter.RvItemAlbumViewHolder

/**
 * Created by Wisn on 2020/6/6 下午6:14.
 */
class OnLineAlbumAdapter(var callback :CallBack, var data: MutableList<UserDirBean> = ArrayList()) : RecyclerView.Adapter<RvItemAlbumViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RvItemAlbumViewHolder {
        return RvItemAlbumViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.rv_item_album, parent, false))
    }

    override fun onBindViewHolder(holder: RvItemAlbumViewHolder, position: Int) {
        val get = data.get(position)
        ImageAdapter.setImageSha1(holder.iv_header, get.sha1_pre)
        holder.name.setText(get.filename)
        holder.des.setText(get.createattimestr)
        if (get.isShare == 1) {
            holder.share.visibility = View.VISIBLE
            if (get.isShareFromMe == 1) {
                holder.share.text = "我的共享相册"
            } else {
                holder.share.text = "${get.ShareFrom} 共享相册"
            }
        } else {
            holder.share.visibility = View.GONE
        }
        holder.itemView.setOnClickListener { callback.AlbumClick(holder.itemView,position,get) }
        holder.itemView.setOnLongClickListener {
            callback.AlbumLongClick(holder.itemView,position,get)
            false
        }
    }


    override fun getItemCount(): Int {
        return data.size
    }

    fun setNewData(data: MutableList<UserDirBean>?) {
        data?.let {
            this.data.clear()
            this.data.addAll(it)
        }
        notifyDataSetChanged()
    }

    fun getItem(index: Int): UserDirBean? {

        try {
            return data.get(index)
        } catch (e: Exception) {
            return null
        }
    }


    class RvItemAlbumViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var iv_header: ImageView = itemView.findViewById(R.id.iv_header)
        var name: TextView = itemView.findViewById(R.id.name)
        var des: TextView = itemView.findViewById(R.id.des)
        var share: TextView = itemView.findViewById(R.id.share)
    }

    interface CallBack{
        fun AlbumLongClick(itemView:View, position: Int, item :UserDirBean)
        fun AlbumClick(itemView:View, position: Int, item :UserDirBean)
    }
}