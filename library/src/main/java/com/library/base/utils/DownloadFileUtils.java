package com.library.base.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.transition.Transition;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;

/**
 * Created by wisn on 2017/9/8.
 */

public class DownloadFileUtils {
    public static final String IMAGEPATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + File.separator;

    public static final String TAG = "CameraFileUtils";

    public static void downloadPicture(final Context context, final String url, boolean isShowTip) {
        Glide.with(context).downloadOnly().load(url).into(new FileTarget() {
            @Override
            public void onLoadStarted(@Nullable Drawable placeholder) {
                super.onLoadStarted(placeholder);
                if (isShowTip) {
//                    Toast..show("正在保存中...");
                }
            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                super.onLoadFailed(errorDrawable);
                if (isShowTip) {
//                    ToastLibUtils.show("保存失败");
                    Toast.makeText(context, "图片保存失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onResourceReady(@NonNull File resource, @Nullable Transition<? super File> transition) {
                super.onResourceReady(resource, transition);
                try {
                    Toast.makeText(context, "图片已经保存", Toast.LENGTH_SHORT).show();
//                    final String path = Environment.getExternalStorageDirectory() + "/" + folderName + "/";
                    saveFileAndUpdateAlbum(resource.getAbsolutePath(), IMAGEPATH, context);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 保存图片文件到相册，并通知相册更新
     *
     * @param oldFilePath
     * @param targetPathDir
     * @param context
     */
    public static void saveFileAndUpdateAlbum(String oldFilePath, String targetPathDir, Context context) {
        try {
            File oldFile = new File(oldFilePath);
            if (!oldFile.exists()) {
                return;
            }
            String mimeType = getImageTypeWithMime(oldFile.getAbsolutePath());
            String newFileName = System.currentTimeMillis() + "." + mimeType;
            String newPath = targetPathDir + newFileName;
            boolean result = copyFile(oldFile, new File(newPath));
            if (result) {
                //兼容华为不能扫描出来的情况 或者扫描出来两条记录的问题
                //  MediaStore.Images.Media.insertImage(context.getApplicationContext().getContentResolver(), newPath, name, "");
                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(newPath)));
                MediaScannerConnection.scanFile(context.getApplicationContext(),
                        new String[]{newPath},
                        new String[]{mimeType},
                        new MediaScannerConnection.OnScanCompletedListener() {
                            @Override
                            public void onScanCompleted(String path, Uri uri) {
                            }
                        });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 获取文件格式
     *
     * @param path
     * @return
     */
    public static String getImageTypeWithMime(String path) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);
            String type = options.outMimeType;
            // ”image/png”、”image/jpeg”、”image/gif”
            if (TextUtils.isEmpty(type)) {
                type = "";
            } else {
                type = type.substring(6);
            }
            return type;
        } catch (Exception e) {
            e.printStackTrace();
            return "jpg";
        }
    }

    /**
     * 格式化文件大小
     *
     * @param size
     * @return
     */
    public static String getReadableFileSize(long size) {
        if (size <= 0) {
            return "0";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }


    /**
     * 拷贝单个文件到指定文件
     *
     * @param fromFile
     * @param toFile
     * @return
     */
    public static boolean copyFile(File fromFile, File toFile) {
        if (fromFile == null || toFile == null) {
            return false;
        }
        try {
            /*if (!toFile.exists()) {
                toFile.createNewFile();
            }*/
            return copyFileStream(new FileInputStream(fromFile), new FileOutputStream(toFile));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }



    /**
     * 拷贝流
     *
     * @param inputStream
     * @param outputStream
     * @return
     */
    public static boolean copyFileStream(InputStream inputStream, OutputStream outputStream) {
        try {
            int index = 0;
            byte[] bytes = new byte[1024];
            while ((index = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, index);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (outputStream != null) {
                        outputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }


}
