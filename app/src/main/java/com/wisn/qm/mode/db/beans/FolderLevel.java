package com.wisn.qm.mode.db.beans;

import java.util.HashMap;

public class FolderLevel {
    private static final FolderLevel folderLevel = new FolderLevel();

    private FolderLevel() {
        map.put("全部相册", 0);
        map.put("camera", 10);
        map.put("dcim", 20);

        map.put("weixin", 30);
        map.put("screenshots", 40);
        map.put("weixinwork", 50);

        map.put("pictures", 60);
        map.put("download", 70);
        map.put("documents", 80);
        map.put("movies", 90);
    }

    private HashMap<String, Integer> map = new HashMap<>();

    public static FolderLevel getFolderLevel() {
        return folderLevel;
    }

    public int getLevel(String foldername) {
        String s = foldername.toLowerCase();
        Integer level = map.get(s);
        if (level == null) {
            level = 10000;
        }
        return level;
    }
}
