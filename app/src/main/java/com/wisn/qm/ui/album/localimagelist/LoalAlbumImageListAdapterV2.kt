package com.wisn.qm.ui.album.localimagelist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.library.base.utils.GlideUtils
import com.qmuiteam.qmui.kotlin.onClick
import com.wisn.qm.R
import com.wisn.qm.mode.beans.FileType
import com.wisn.qm.mode.db.beans.MediaInfo
import com.wisn.qm.ui.preview.PreviewMediaFragment
import java.util.ArrayList

/**
 * Created by Wisn on 2020/6/6 下午6:14.
 */

class LoalAlbumImageListAdapterV2(var pictureController: LocalCallBack) : BaseMultiItemQuickAdapter<MediaInfo, LocalAlbumViewHolder>() {
    open var isSelectModel: Boolean = false
    open var isSelectAll: Boolean = false
    var map: HashMap<Long, MediaInfo> = HashMap()

    fun deleteSelect(){
        val values = map.values;
        for(median in values){
            remove(median)
        }
        map.clear()
        isSelectAll=false
    }

    fun resetSelect(){
        map.clear()
        isSelectAll=false
        notifyDataSetChanged()
    }

    fun updateSelect(isSelectModel: Boolean?) {
        isSelectModel?.let {
            this.isSelectModel = isSelectModel
            pictureController.changeSelectData(false, isSelectModel, isSelectAll,false, null);
        }
        notifyDataSetChanged()
    }

    fun updateSelectAll():Boolean {
        this.isSelectAll = !isSelectAll
        if (isSelectAll) {
            for (mediainfo in data) {
                map.put(mediainfo.id!!, mediainfo)
            }
        } else {
            map.clear()
        }
        pictureController.changeSelectData(false, isSelectModel, isSelectAll,isSelectAll, data);
        notifyDataSetChanged()
        return this.isSelectAll
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocalAlbumViewHolder {
        val from = LayoutInflater.from(parent.context)
        if (viewType == FileType.ImageViewItem||viewType == FileType.VideoViewItem) {
            return LocalAlbumViewHolder(
                from.inflate(
                    R.layout.rv_item_picture_image,
                    parent,
                    false
                )
            )
        }
        return super.onCreateViewHolder(parent, viewType)
    }


    override fun convert(viewhoder: LocalAlbumViewHolder, item: MediaInfo) {
        val adapterPosition = viewhoder.adapterPosition
        if (item.itemType == FileType.ImageViewItem || item.itemType == FileType.VideoViewItem) {
//            LogUtils.d(item.filePath)
            viewhoder.image?.let {
                GlideUtils.loadFile(item.filePath!!, it)
//                Glide.with(context).load(File(item.filePath!!))
//                        .apply(RequestOptions())
//                        .into(it)
                viewhoder.image?.setOnLongClickListener(View.OnLongClickListener {
                    if (!isSelectModel) {
                        //不是选择模式
                        map.clear()
                        pictureController.showPictureControl(true)
//                        item.isSelect = true
                        map.put(item.id!!, item)
                        this.isSelectModel = true
                        notifyDataSetChanged()
                        if (map.size == data.size) {
                            isSelectAll = true
                        }
                        val arrayList = ArrayList<MediaInfo>(1);
                        arrayList.add(item)
                        pictureController.changeSelectData(
                            true,
                            isSelectModel,
                            isSelectAll,
                            true,
                            arrayList
                        )
                    }
                    return@OnLongClickListener false
                })
                viewhoder.image?.onClick {
                    if (isSelectModel) {
                        var target = map.get(item.id!!)
                        var isSelect = false;
                        if (target == null) {
                            map.put(item.id!!, item)
                            isSelect = true;
                        } else {
                            map.remove(item.id!!)
                        }
                        notifyDataSetChanged()
                        if (!isSelect) {
                            isSelectAll = false;
                        } else {
                            if (map.size == data.size) {
                                isSelectAll = true
                            }
                        }
                        val arrayList = ArrayList<MediaInfo>(1);
                        arrayList.add(item)
                        pictureController.changeSelectData(
                            false,
                            isSelectModel,
                            isSelectAll,
                            isSelect,
                            arrayList
                        )
                    } else {
                        //查看大图
                        val previewFragment = PreviewMediaFragment(data, adapterPosition)
                        pictureController.getQMUIFragment().startFragment(previewFragment)
                    }
                }
                if (isSelectModel) {
                    viewhoder.iv_select?.visibility = View.VISIBLE
                    var isSelect = map.containsKey(item.id!!)
                    if (!isSelect) {
                        viewhoder.iv_select?.setBackgroundResource(R.mipmap.ic_image_unselected)
                    } else {
                        viewhoder.iv_select?.setBackgroundResource(R.mipmap.ic_image_selected)
                    }
                } else {
                    viewhoder.iv_select?.visibility = View.GONE
                }

            }
            if (item.uploadStatus == FileType.MediainfoStatus_uploadSuccess) {
                viewhoder.iv_isexist?.visibility = View.VISIBLE
            } else {
                viewhoder.iv_isexist?.visibility = View.GONE
            }
            if (item.isVideo!!) {
                viewhoder.video_time?.visibility = View.VISIBLE
                viewhoder.video_time?.setText(item.timestr)
            } else {
                viewhoder.video_time?.visibility = View.GONE
            }
//            dataBinding?.showpath?.text = " " + item.id

        }
    }


}