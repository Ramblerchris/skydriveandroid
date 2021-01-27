package com.wisn.qm.ui.album.newalbum

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.library.base.utils.GlideUtils
import com.wisn.qm.R
import com.wisn.qm.mode.db.beans.MediaInfo

class NewAlbumAdapter1(var clickItem: ClickItem, var data: MutableList<MediaInfo> = ArrayList()) : RecyclerView.Adapter<SelectFileViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectFileViewHolder {
        return SelectFileViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.rv_item_newalbum_media, parent, false))
    }

    private val i = -100

    override fun onBindViewHolder(holder: SelectFileViewHolder, position: Int) {
        val get = data.get(position)
        if (i === get.itemType) {
            holder.newalbum_video_time.visibility = View.GONE
            holder.newalbum_iv_select.visibility = View.GONE
            holder.newalbum_image.visibility = View.VISIBLE
            holder.newalbum_image.setImageResource(R.mipmap.cloud_album_ic_auto_add_face)
        } else {
            get.filePath?.let {
                GlideUtils.loadFile(it, holder.newalbum_image)
            }
            if (get.isVideo!!) {
                holder.newalbum_video_time.visibility = View.VISIBLE
                holder.newalbum_video_time.setText(get.timestr)
            } else {
                holder.newalbum_video_time.visibility = View.GONE
            }
            holder.newalbum_iv_select.visibility = View.VISIBLE
            holder.newalbum_iv_select.setOnClickListener {
                data.removeAt(position)
                notifyDataSetChanged()
            }
        }
        holder.newalbum_image.setOnClickListener {
            clickItem?.click(get.itemType == i, position, get)
        }
    }


    override fun getItemCount(): Int {
        return data.size
    }

    fun setNewData(data: MutableList<MediaInfo>?) {
        if (this.data.size >= 1) {
            this.data.removeAt(this.data.size - 1)
        }
        data?.let { this.data.addAll(it) }
        this.data.add(MediaInfo(i))
        notifyDataSetChanged()
    }

    fun getSelectDate(): MutableList<MediaInfo>? {
      /*  if (this.data.size >= 1) {
            val get = this.data.get(this.data.size - 1)
            if (get.itemType == i) {
                return this.data.subList(0, this.data.size - 1)
            } else {
                return this.data
            }
        }*/
        return this.data
    }

}

class SelectFileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var newalbum_image: ImageView = itemView.findViewById(R.id.newalbum_image)
    var newalbum_iv_select: ImageView = itemView.findViewById(R.id.newalbum_iv_select)
    var newalbum_video_time: TextView = itemView.findViewById(R.id.newalbum_video_time)
}

interface ClickItem {
    fun click(isadd: Boolean, position: Int, fileBean: MediaInfo)
}