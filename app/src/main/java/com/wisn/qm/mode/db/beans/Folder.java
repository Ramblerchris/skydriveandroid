package com.wisn.qm.mode.db.beans;


import android.text.TextUtils;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.wisn.qm.mode.beans.FileType;

import java.util.ArrayList;

/**
 * 图片文件夹实体类
 */
public class Folder implements MultiItemEntity {

    public String name;
    public int level;
    public ArrayList<MediaInfo> images;
    public int type = FileType.Album;

    public Folder(String name) {
        this.name = name;
        this.level=FolderLevel.getFolderLevel().getLevel(name);
    }

    public Folder(String name, ArrayList<MediaInfo> images) {
        this.name = name;
        this.images = images;
    }

    public void addImage(MediaInfo image) {
        if (image != null && !TextUtils.isEmpty(image.getFilePath())) {
            if (images == null) {
                images = new ArrayList<>();
            }
            images.add(image);
        }
    }

    @Override
    public String toString() {
        return "Folder{" +
                "name='" + name + '\'' +
                ", images=" + images +
                '}';
    }

    @Override
    public int getItemType() {
        return type;
    }
}
