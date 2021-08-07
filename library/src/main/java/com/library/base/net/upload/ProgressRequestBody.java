package com.library.base.net.upload;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

public class ProgressRequestBody extends RequestBody {
    private final ProgressRequestBodyUploadListener progressRequestBodyUploadListener;
    private final RequestBody requestBody;
    private BufferedSink bufferedSink;
    private String tag;

    public ProgressRequestBody(String tag, ProgressRequestBodyUploadListener progressRequestBodyUploadListener, File file, String mineType) {
        this.tag = tag;
        this.progressRequestBodyUploadListener = progressRequestBodyUploadListener;
        if (TextUtils.isEmpty(mineType)) {
            mineType = "multipart/form-data";
        }
        this.requestBody = RequestBody.create(MediaType.parse(mineType), file);
    }

    @Nullable
    @Override
    public MediaType contentType() {
        return requestBody.contentType();
    }

    @Override
    public long contentLength() throws IOException {
        return requestBody.contentLength();
    }

    //关键方法
    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        if (null == bufferedSink) bufferedSink = Okio.buffer(sink(sink));
        requestBody.writeTo(bufferedSink);
        //必须调用flush，否则最后一部分数据可能不会被写入
        bufferedSink.flush();
    }

    private Sink sink(Sink sink) {
        return new ForwardingSink(sink) {
            long bytesWriting = 0l;
            long contentLength = 0l;

            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                super.write(source, byteCount);
                if (0 == contentLength) contentLength = contentLength();
                bytesWriting += byteCount;
                if (progressRequestBodyUploadListener != null) {
                    //调用接口，把上传文件的进度传过去
                    progressRequestBodyUploadListener.onProgress(tag, bytesWriting, contentLength);
                }
            }
        };
    }
}
