package com.wisn.qm.ui.album.localimagelist

import android.view.View
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.library.base.utils.GlideUtils
import com.qmuiteam.qmui.kotlin.onClick
import com.wisn.qm.R
import com.wisn.qm.databinding.RvItemPictureImageBinding
import com.wisn.qm.databinding.RvItemPictureTitleBinding
import com.wisn.qm.mode.beans.FileType
import com.wisn.qm.mode.db.beans.MediaInfo
import com.wisn.qm.ui.home.BaseDataBindlingViewHolder
import com.wisn.qm.ui.preview.PreviewMediaFragment
import java.util.ArrayList

/**
 * Created by Wisn on 2020/6/6 下午6:14.
 */

class LoalAlbumImageListAdapterV2(pictureController: LocalCallBack?) : BaseMultiItemQuickAdapter<MediaInfo, BaseDataBindlingViewHolder>() {
    protected var pictureController: LocalCallBack
    open var isSelectModel: Boolean = false
    open var isSelectAll: Boolean = false
    var map: HashMap<Long, MediaInfo> = HashMap()

    init {
        addItemType(FileType.TimeTitle, R.layout.rv_item_picture_title)
        addItemType(FileType.ImageViewItem, R.layout.rv_item_picture_image)
        addItemType(FileType.VideoViewItem, R.layout.rv_item_picture_image)
        this.pictureController = pictureController!!
    }

    fun deleteSelect(){
        val values = map.values;
        for(median in values){
            remove(median)
        }
        map.clear()
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

    /**
     * （可选重写）当 item 的 ViewHolder创建完毕后，执行此方法。
     * 可在此对 ViewHolder 进行处理，例如进行 DataBinding 绑定 view
     *
     * @param viewHolder VH
     * @param viewType Int
     */
    override fun onItemViewHolderCreated(viewHolder: BaseDataBindlingViewHolder, viewType: Int) {
        super.onItemViewHolderCreated(viewHolder, viewType)
        if (viewType == FileType.TimeTitle) {
            viewHolder.setDataBinding<RvItemPictureImageBinding>(viewHolder.itemView)
        } else if (viewType == FileType.ImageViewItem||viewType == FileType.VideoViewItem) {
            viewHolder.setDataBinding<RvItemPictureTitleBinding>(viewHolder.itemView)
        }
    }


    override fun convert(holder: BaseDataBindlingViewHolder, item: MediaInfo) {
        val adapterPosition = holder.adapterPosition
        if (item.itemType == FileType.TimeTitle) {
//         val  binding  =holder.dataBinding as? RvItemPictureImageBinding
            val dataBinding = holder.getDataBinding<RvItemPictureTitleBinding>()
//            dataBinding?.tvTitle?.text = item.na.toString()

        } else {
            val dataBinding = holder.getDataBinding<RvItemPictureImageBinding>()
//            LogUtils.d(item.filePath)
            dataBinding?.image?.let {
                GlideUtils.loadFile(item.filePath!!,it)
//                Glide.with(context).load(File(item.filePath!!))
//                        .apply(RequestOptions())
//                        .into(it)
                dataBinding.image.setOnLongClickListener(View.OnLongClickListener {
                    if (!isSelectModel) {
                        //不是选择模式
                        map.clear()
                        pictureController.showPictureControl(true)
//                        item.isSelect = true
                        map.put(item.id!!, item)
                        this.isSelectModel=true
                        notifyDataSetChanged()
                        if (map.size == data.size) {
                            isSelectAll = true
                        }
                        val arrayList = ArrayList<MediaInfo>(1);
                        arrayList.add(item)
                        pictureController.changeSelectData(true, isSelectModel,isSelectAll, true,arrayList)
                    }
                    return@OnLongClickListener false
                })
                dataBinding.image.onClick {
                    if (isSelectModel) {
                        var target = map.get(item.id!!)
                        var isSelect=false;
                        if (target == null) {
                            map.put(item.id!!, item)
                            isSelect=true;
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
                        pictureController.changeSelectData(false, isSelectModel,isSelectAll, isSelect, arrayList)
                    } else {
                        //查看大图
                        val previewFragment = PreviewMediaFragment(data, adapterPosition)
                        pictureController.getQMUIFragment().startFragment(previewFragment)
                    }
                }
                if (isSelectModel) {
                    dataBinding.ivSelect.visibility = View.VISIBLE
                    var isSelect = map.containsKey(item.id!!)
                    if (!isSelect) {
                        dataBinding.ivSelect.setBackgroundResource(R.mipmap.ic_image_unselected)
                    } else {
                        dataBinding.ivSelect.setBackgroundResource(R.mipmap.ic_image_selected)
                    }
                } else {
                    dataBinding.ivSelect.visibility = View.GONE
                }

            }
            if (item.uploadStatus == FileType.MediainfoStatus_uploadSuccess) {
                dataBinding?.ivIsexist?.visibility = View.VISIBLE
            } else {
                dataBinding?.ivIsexist?.visibility = View.GONE
            }
            if (item.isVideo!!) {
                dataBinding?.videoTime?.visibility = View.VISIBLE
                dataBinding?.videoTime?.setText(item.timestr)
            } else {
                dataBinding?.videoTime?.visibility = View.GONE
            }
//            dataBinding?.showpath?.text = " " + item.id

        }
    }


}