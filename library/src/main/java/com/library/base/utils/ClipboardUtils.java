package com.library.base.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.TextUtils;

public class ClipboardUtils {


    public static boolean copy(Context context, String string) {
        if (context == null || TextUtils.isEmpty(string)) {
            return false;
        }
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) {
            ClipData mClipData = ClipData.newPlainText("Label", string);
            cm.setPrimaryClip(mClipData);
            return true;
        }
        return false;
    }
}
