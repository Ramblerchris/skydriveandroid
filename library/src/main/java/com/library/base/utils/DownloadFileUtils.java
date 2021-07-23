package com.library.base.utils;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.exifinterface.media.ExifInterface;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.transition.Transition;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
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
     * 如果是缓存中的文件 会删除
     *
     * @param context
     * @param filepath
     */
    public static void deleteCacheFile(Application context, String filepath) {
        try {
            if (TextUtils.isEmpty(filepath) || !filepath.startsWith(getImageSaveRootPath(context))) {
                return;
            }
            File file = new File(filepath);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取指定的缓存根目录
     *
     * @param context
     * @return
     */
    public static String getImageSaveRootPath(Context context) {
        try {
//            String path = context.getExternalCacheDir().getAbsolutePath() + File.separator + "upload" + File.separator;
            String path = context.getExternalFilesDir(null).getAbsolutePath() + File.separator + "upload" + File.separator;
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }
            return path;
        } catch (Exception e) {
            e.printStackTrace();
            try {
//                return context.getExternalCacheDir().getAbsolutePath();
                return context.getFilesDir().getAbsolutePath();
            } catch (Exception exception) {
                exception.printStackTrace();
                return Environment.getExternalStorageDirectory() +File.separator+ "hxjf"+ File.separator;
            }
        }
    }

    /**
     * 获取公共目录
     *
     * @return
     */
    public static String getPublicDirPath() {
        try {
            String path =   Environment.getExternalStorageDirectory() +File.separator+ "hxjf"+ File.separator;
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }
            return path;
        } catch (Exception e) {
            e.printStackTrace();
            return Environment.getExternalStorageDirectory().getAbsolutePath();
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
     * 保存bitmap 到文件
     *
     * @param path
     * @param bitmap
     */
    public static void savePictureByBitmap(String path, Bitmap bitmap) {
        savePictureByBitmap(path, bitmap, 100);
    }

    /**
     * 保存bitmap 到文件
     *
     * @param path
     * @param bitmap
     * @param quality
     */
    public static void savePictureByBitmap(String path, Bitmap bitmap, int quality) {
        File imgFile = new File(path);
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        try {
            fos = new FileOutputStream(imgFile);
            bos = new BufferedOutputStream(fos);
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos);
        } catch (Exception error) {
            error.printStackTrace();
        } finally {
            try {
                if (bos != null) {
                    bos.flush();
                    bos.close();
                }

                if (fos != null) {
                    fos.flush();
                    fos.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取文件名称
     *
     * @return
     */
    public static String getFileName() {
        return System.currentTimeMillis() + ".jpg";
    }

    /**
     * 获取压缩文件名称
     *
     * @param oldFileName
     * @return
     */
    public static String getFileNameCompress(String oldFileName) {
        if (TextUtils.isEmpty(oldFileName)) {
            return getFileName();
        }
        int i = oldFileName.lastIndexOf(".");
        if (i != -1) {
            return oldFileName.substring(0, i) + "_compress.jpg";
        } else {
            return oldFileName + "_compress.jpg";
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
