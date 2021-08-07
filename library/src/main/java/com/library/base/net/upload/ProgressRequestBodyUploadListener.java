package com.library.base.net.upload;

public interface ProgressRequestBodyUploadListener {
    /**
     *
     * @param bytesWriting 已经写的字节数
     * @param totalBytes   文件的总字节数
     */
    void onProgress(String tag,long bytesWriting, long totalBytes);
}
