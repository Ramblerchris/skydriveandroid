package com.wisn.qm.task;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.library.base.glide.progress.OnProgressListener;
import com.library.base.net.upload.ProgressRequestBodyUploadListener;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class UploadFileProgressManager implements ProgressRequestBodyUploadListener {
    private static final Map<String, OnProgressListener> listenersMap =
            Collections.synchronizedMap(new HashMap<String, OnProgressListener>());
    private static UploadFileProgressManager uploadFileProgressManager = new UploadFileProgressManager();
    private static final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    private UploadFileProgressManager() {
    }

    public static UploadFileProgressManager getInstance() {
        return uploadFileProgressManager;
    }

    public void addListener(String url, OnProgressListener listener) {
        if (!TextUtils.isEmpty(url) && listener != null) {
            listenersMap.put(url, listener);
            listener.onProgress(url, false, 1, 0, 0);
        }
    }

    public void removeListener(String url) {
        if (!TextUtils.isEmpty(url)) {
            listenersMap.remove(url);
        }
    }

    public  OnProgressListener getProgressListener(String url) {
        if (TextUtils.isEmpty(url) || listenersMap == null || listenersMap.size() == 0) {
            return null;
        }

        OnProgressListener listenerWeakReference = listenersMap.get(url);
        return listenerWeakReference;
    }

    @Override
    public void onProgress(String tag, long bytesWriting, long totalBytes) {
        OnProgressListener onProgressListener = getProgressListener(tag);
        if (onProgressListener != null) {
            int percentage = (int) ((bytesWriting * 1f / totalBytes) * 100f);
            boolean isComplete = percentage >= 100;
            Log.d(
                    "UploadFile",
                    "onProgress"
            );
            mainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    onProgressListener.onProgress(tag, isComplete, percentage, bytesWriting, totalBytes); }
            });
            if (isComplete) {
                removeListener(tag);
            }
        }
    }
}
