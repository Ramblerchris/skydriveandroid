package com.wisn.qm.ui.upload

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.library.base.utils.GlideUtils
import com.wisn.qm.R
import com.wisn.qm.databinding.RvItemUploadlistBinding
import com.wisn.qm.mode.db.beans.UploadBean

/**
 * Created by Wisn on 2020/6/6 下午6:14.
 */

class UploadListAdapter : BaseQuickAdapter<UploadBean, BaseDataBindingHolder<RvItemUploadlistBinding>>(R.layout.rv_item_uploadlist) {

    override fun convert(holder: BaseDataBindingHolder<RvItemUploadlistBinding>, item: UploadBean) {
        holder.dataBinding?.itemData = item
        holder.dataBinding?.executePendingBindings()
        GlideUtils.loadFile(item.filePath!!,holder.dataBinding!!.ivHeader)
//        Glide.with(context).load(File(item.filePath!!))
//                .apply(RequestOptions())
//                .into()
    }

}