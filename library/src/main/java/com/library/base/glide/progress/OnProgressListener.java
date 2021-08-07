package com.library.base.glide.progress;


public interface OnProgressListener {
    void onProgress(String tag, boolean isComplete, int percentage, long bytesRead, long totalBytes);
}