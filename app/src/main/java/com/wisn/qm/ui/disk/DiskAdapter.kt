package com.wisn.qm.ui.disk

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.VibrateUtils
import com.wisn.qm.R
import com.wisn.qm.mode.beans.FileBean
import com.wisn.qm.mode.db.beans.UserDirBean

class DiskAdapter(var clickItem: ClickItem, var data: MutableList<UserDirBean>) : RecyclerView.Adapter<SelectFileViewHolder>() {

    var selectList: MutableList<UserDirBean> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectFileViewHolder {
        return SelectFileViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.rv_item_disk, parent, false))
    }

    override fun onBindViewHolder(holder: SelectFileViewHolder, position: Int) {
        val get = data.get(position)
        holder.setData(get)
        holder.itemView.setOnClickListener {
            clickItem.click(position, get)
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
    fun setData(fileBean: UserDirBean) {
        textView.setText(fileBean.filename)
        des.setText(fileBean.createattimestr)
        select.visibility = View.INVISIBLE

        if (fileBean.type==1) {
            imageView.setImageResource(R.mipmap.icon_select_dir2)
        } else {
            imageView.setImageResource(R.mipmap.icon_select_file2)
        }
    }
}

interface ClickItem {
    fun click(position: Int, fileBean: UserDirBean)
}