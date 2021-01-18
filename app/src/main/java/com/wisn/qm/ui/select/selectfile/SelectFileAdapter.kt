package com.wisn.qm.ui.select.selectfile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wisn.qm.R
import com.wisn.qm.mode.beans.FileBean

class SelectFileAdapter(var clickItem:ClickItem,var data: MutableList<FileBean>) : RecyclerView.Adapter<SelectFileViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectFileViewHolder {
        return SelectFileViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.rv_item_selectfile, parent, false))
    }

    override fun onBindViewHolder(holder: SelectFileViewHolder, position: Int) {
        holder.setData(data.get(position))
        holder.itemView.setOnClickListener {
            clickItem.click(position,data.get(position))
        }
    }


    override fun getItemCount(): Int {
        return data.size
    }

    fun setNewData( data: MutableList<FileBean>){
        this.data=data
        notifyDataSetChanged()
    }

}

class SelectFileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var imageView: ImageView = itemView.findViewById(R.id.imageView)
    var select: ImageView = itemView.findViewById(R.id.select)
    var textView: TextView = itemView.findViewById(R.id.textView)
    fun setData(fileBean: FileBean) {
        textView.setText(fileBean.fileName)
        if (fileBean.isDir) {
            imageView.setImageResource(R.mipmap.icon_select_dir2)
        } else {
            imageView.setImageResource(R.mipmap.icon_select_file2)
        }
    }
}
interface ClickItem{
    fun click(position:Int,fileBean: FileBean)
}