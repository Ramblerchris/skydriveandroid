package com.wisn.qm.ui.album.details

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.library.base.config.Constant
import com.library.base.utils.GlideUtils
import com.qmuiteam.qmui.kotlin.onClick
import com.wisn.qm.R
import com.wisn.qm.mode.beans.FileType
import com.wisn.qm.mode.db.beans.UserDirBean
import com.wisn.qm.ui.album.EditAlbumDetails
import com.wisn.qm.ui.preview.PreviewMediaFragment

/**
 * Created by Wisn on 2020/6/6 下午6:14.
 */

class AlbumDetailsAdapter(
    var gridLayoutManager: GridLayoutManager,
    var editAlbumDetails: EditAlbumDetails,
    var albumDetailsFragment: AlbumDetailsFragment
) : BaseMultiItemQuickAdapter<UserDirBean, AlbumDetailsViewHolder>(), LoadMoreModule {
    var isSelectModel: Boolean = false
    var map: HashMap<Long, Boolean> = HashMap()

    init {
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                if(position<data.size){
                    val get = getItem(position)
                    return when (get.itemType) {
                        FileType.ImageViewItem -> 1
                        FileType.TimeTitle -> 3
                        else -> 1
                    }
                }
                return 3
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumDetailsViewHolder {
        val from = LayoutInflater.from(parent.context)
        if (viewType == FileType.ImageViewItem||viewType == FileType.VideoViewItem) {
            return AlbumDetailsViewHolder(
                from.inflate(
                    R.layout.rv_item_album_detail_media,
                    parent,
                    false
                )
            )
        }
        return super.onCreateViewHolder(parent, viewType)
    }

    fun updateSelect(isSelectModel: Boolean?) {
        isSelectModel?.let {
            this.isSelectModel = isSelectModel
            editAlbumDetails.isShowEdit(false)
        }
        notifyDataSetChanged()
    }


    override fun convert(viewhoder: AlbumDetailsViewHolder, item: UserDirBean) {
        val adapterPosition = viewhoder.adapterPosition
        if (item.itemType == FileType.ImageViewItem || item.itemType == FileType.VideoViewItem) {
//            viewHolder.setDataBinding<RvItemAlbumDetailMediaBinding>(viewHolder.itemView)
            viewhoder.image?.let {
//                Glide.with(context).load(Constant.getImageUrl(item.sha1!!))
//                    .apply(RequestOptions())
//                    .into(it)
                GlideUtils.loadUrl(Constant.getImageUrlThumb(item.sha1!!),it)
                viewhoder.image?.setOnLongClickListener(View.OnLongClickListener {
                    if (!isSelectModel) {
                        map.clear()
                        map.put(item.id!!, true)
                        isSelectModel = true;
                        notifyDataSetChanged()
                        editAlbumDetails.isShowEdit(true)
                        editAlbumDetails.changeSelectData(true, true, item)
                    }
                    return@OnLongClickListener false
                })
                viewhoder.image?.onClick {
                    if (isSelectModel) {
                        var isSelect = map.get(item.id!!)
                        if (isSelect == null) {
                            isSelect = true;
                        } else {
                            isSelect = !isSelect
                        }
                        map.put(item.id!!, isSelect)
                        notifyItemChanged(adapterPosition)
                        editAlbumDetails.changeSelectData(false, isSelect, item)
                    } else {
                        //查看大图
                        val netPreviewFragment = PreviewMediaFragment(data, adapterPosition)
                        albumDetailsFragment.startFragment(netPreviewFragment)
//                        pictureController.getHomeFragment().startFragment(previewFragment)
                    }
                }
            }
            if (item.ftype == 1) {
                viewhoder.video_time?.visibility = View.VISIBLE
                viewhoder.video_time?.setText(item.getVideoDurationFor())
            } else {
                viewhoder.video_time?.visibility = View.GONE
            }

            if (isSelectModel) {
                viewhoder.iv_select?.visibility = View.VISIBLE
                var isSelect = map.get(item.id!!)
                if (isSelect == null || !isSelect!!) {
                    viewhoder.iv_select?.setBackgroundResource(R.mipmap.ic_image_unselected)
                } else {
                    viewhoder.iv_select?.setBackgroundResource(R.mipmap.ic_image_selected)
                }
            } else {
                viewhoder.iv_select?.visibility = View.GONE
            }
        }
    }


}