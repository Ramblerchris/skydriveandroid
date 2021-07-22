package com.wisn.qm.ui.preview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wisn.qm.R
import com.wisn.qm.mode.beans.FileType
import com.wisn.qm.mode.beans.PreviewImage
import com.wisn.qm.ui.preview.viewholder.BasePreviewHolder
import com.wisn.qm.ui.preview.viewholder.PreviewImageViewHolder
import com.wisn.qm.ui.preview.viewholder.PreviewVideoViewHolder

class PreviewMediaAdapter(var data: MutableList<out PreviewImage>, var previewCallback: PreviewMediaCallback) : RecyclerView.Adapter<BasePreviewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BasePreviewHolder {
        if (viewType == FileType.ImageViewItem) {
            return PreviewImageViewHolder(parent.context, LayoutInflater.from(parent.context).inflate(R.layout.rv_item_preview_localimage, parent, false), previewCallback)
        } else if (viewType == FileType.VideoViewItem) {
            return PreviewVideoViewHolder(parent.context, LayoutInflater.from(parent.context).inflate(R.layout.rv_item_preview_netvideo, parent, false), previewCallback)
        }
        return super.createViewHolder(parent,viewType)
    }

    override fun onBindViewHolder(holder: BasePreviewHolder, position: Int) {
        if (getItemViewType(position) == FileType.ImageViewItem) {
            holder.loadImage(position,data.get(position))
        } else {
            holder.loadVideo(position,data.get(position))
        }
    }

    override fun onViewDetachedFromWindow(holder: BasePreviewHolder) {
        super.onViewDetachedFromWindow(holder)
        if (holder is PreviewVideoViewHolder) {
            var position = holder.adapterPosition;
            holder.releaseVideo(position, data.get(position));
        }

    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun getItemViewType(position: Int): Int {
        return data.get(position).itemType
    }


}