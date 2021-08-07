package com.wisn.qm.ui.upload

import android.view.LayoutInflater
import android.view.ViewGroup
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.wisn.qm.R
import com.wisn.qm.mode.beans.FileType
import com.wisn.qm.mode.db.beans.UploadBean

/**
 * Created by Wisn on 2020/6/6 下午6:14.
 */

class UploadListAdapter : BaseMultiItemQuickAdapter<UploadBean, BaseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val from = LayoutInflater.from(parent.context)
        if (viewType ==FileType.UploadInfoProgressItem) {
            return UploadViewHolder(
                from.inflate(
                    R.layout.rv_item_uploadlist,
                    parent,
                    false
                )
            )
        }
        return super.onCreateViewHolder(parent, viewType)
    }

    override fun convert(viewhoder: BaseViewHolder, item: UploadBean) {
        val adapterPosition = viewhoder.adapterPosition
        if (item.itemType ==FileType.UploadInfoProgressItem) {
          var holder : UploadViewHolder=viewhoder as  UploadViewHolder
            holder.loadInfo(context,item);
        }

    }
   /* (R.layout.rv_item_uploadlist) {

        override fun convert(holder: BaseDataBindingHolder<RvItemUploadlistBinding>, item: UploadBean) {
            holder.dataBinding?.itemData = item
            holder.dataBinding?.executePendingBindings()
            GlideUtils.loadFile(item.filePath!!,holder.dataBinding!!.ivHeader)
//        Glide.with(context).load(File(item.filePath!!))
//                .apply(RequestOptions())
//                .into()
        }

    }*/
}