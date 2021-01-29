package com.wisn.qm.ui.disk

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.library.base.config.Constant
import com.library.base.utils.FormatStrUtils
import com.wisn.qm.R
import com.wisn.qm.mode.db.beans.UserDirBean

class DiskAdapter(var clickItem: ClickItem, var data: MutableList<UserDirBean>) : RecyclerView.Adapter<SelectFileViewHolder>() {

    var selectList: MutableList<UserDirBean> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectFileViewHolder {
        return SelectFileViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.rv_item_disk, parent, false))
    }

    override fun onBindViewHolder(holder: SelectFileViewHolder, position: Int) {
        val fileBean = data.get(position)
        holder.textView.setText(fileBean.filename)
        holder.select.visibility = View.INVISIBLE

        if (fileBean.type == Constant.TypeDir) {
            holder.imageView.setImageResource(R.mipmap.icon_select_dir2)
            holder.des.setText(fileBean.createattimestr)

        } else {
            holder.imageView.setImageResource(R.mipmap.icon_select_file2)
            if (fileBean.size != null) {
                holder.des.setText("${fileBean.createattimestr} ${FormatStrUtils.getFormatDiskSizeStr(fileBean.size!!.toLong())}")
            } else {
                holder.des.setText(fileBean.createattimestr)
            }
        }
        holder.itemView.setOnClickListener {
            clickItem.click(position, fileBean)
        }
        holder.itemView.setOnLongClickListener {
            clickItem.longclick(holder.textView, position, fileBean)
            true
        }
    }


    override fun getItemCount(): Int {
        return data.size
    }

    fun setNewData(data: MutableList<UserDirBean>) {
        this.data = data
        selectList.clear()
        notifyDataSetChanged()
    }

}

class SelectFileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var imageView: ImageView = itemView.findViewById(R.id.imageView)
    var select: ImageView = itemView.findViewById(R.id.select)
    var textView: TextView = itemView.findViewById(R.id.textView)
    var des: TextView = itemView.findViewById(R.id.des)
}

interface ClickItem {
    fun click(position: Int, fileBean: UserDirBean)
    fun longclick(view: View, position: Int, fileBean: UserDirBean)
}