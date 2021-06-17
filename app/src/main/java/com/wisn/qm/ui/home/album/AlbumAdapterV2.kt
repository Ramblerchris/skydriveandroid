package com.wisn.qm.ui.home.album

import android.widget.TextView
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.library.base.utils.GlideUtils
import com.qmuiteam.qmui.kotlin.onClick
import com.qmuiteam.qmui.widget.QMUIRadiusImageView
import com.wisn.qm.R
import com.wisn.qm.mode.beans.FileType
import com.wisn.qm.mode.db.beans.Folder
import com.wisn.qm.ui.album.local.LocalAlbumFragment

/**
 * Created by Wisn on 2020/6/6 下午6:14.
 */

class AlbumAdapterV2(pictureController: PictureCallBack?) : BaseMultiItemQuickAdapter<Folder, BaseViewHolder>() {
    protected var pictureController: PictureCallBack
    open var isSelectModel: Boolean = false

    init {
        addItemType(FileType.Album, R.layout.rv_item_picture_album)
        this.pictureController = pictureController!!
    }

    fun updateSelect(isSelectModel: Boolean?) {
        isSelectModel?.let {
            this.isSelectModel = isSelectModel
            pictureController.changeSelectData(false, isSelectModel, false, null);
        }
        notifyDataSetChanged()
    }



    override fun convert(holder: BaseViewHolder, item: Folder) {
        val adapterPosition = holder.adapterPosition
        if (item.itemType == FileType.Album) {
           var image= holder.getView<QMUIRadiusImageView>(R.id.image)
           var title= holder.getView<TextView>(R.id.title)
           var count= holder.getView<TextView>(R.id.count)
            GlideUtils.loadFile(item.images.get(0).filePath!!,image)
            title.setText(item.name)
            count.setText("${item.images.size}")
            holder.itemView.onClick {
                pictureController.getHomeFragment().startFragment(LocalAlbumFragment(item))
            }
        }

    }


}