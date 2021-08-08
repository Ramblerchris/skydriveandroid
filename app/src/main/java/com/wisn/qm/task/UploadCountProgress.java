package com.wisn.qm.task;

import com.wisn.qm.mode.db.beans.UploadBean;

public class UploadCountProgress {
    public static final int UploadCountProgress_Album=1;
    public static final int UploadCountProgress_Disk=2;
    //1 album 2 disk
    public int type;
    public int sum ;
    public int leftsize=0 ;
    public int uploadcount ;
    public UploadBean currentUploadBean ;
    public boolean isFinish ;

    public UploadCountProgress(int type,int sum ) {
        this.type = type;
        this.sum =sum ;
    }
    public UploadCountProgress(int type,boolean isFinish ) {
        this.type = type;
        this.isFinish = isFinish;
    }

    @Override
    public String toString() {
        return "UploadCountProgress{" +
                "sum=" + sum +
                ", leftsize=" + leftsize +
                ", uploadcount=" + uploadcount +
                ", isFinish=" + isFinish +
                '}';
    }
}
