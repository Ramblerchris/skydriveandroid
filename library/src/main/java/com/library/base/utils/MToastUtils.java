package com.library.base.utils;
import android.content.Context;
import android.graphics.Color;
import android.os.Looper;
import android.text.TextUtils;

import com.coder.zzq.smartshow.toast.SmartToast;
import com.library.R;

/**
 * Android 7.1系统上，Toast会偶现BadTokenException,Google在Android8.0修复了这个bug。
 * Android8.0开始，google及各大厂商都对Toast进行了优化
 * eg:华为麦芒7设备(android 8.1.0)为例，同一个Toast实例，短时间多次调用show方法，Toast会立即消失，而且也不会弹出新的Toast，持续触发，Toast将一直不显示
 */
public class MToastUtils {

    public static void show(String msg) {

    if (TextUtils.isEmpty(msg)) {
        return;
    }

    try {
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            return;
        }
//        SmartToast.show(msg);
        SmartToast
                .classic()
                .config()
                .backgroundColor(Color.parseColor("#99000000"))
                .msgColorResource(R.color.exo_white)
                .msgSize(14)
                .apply()
                .showInCenter(msg);
//            ToastUtils.show(msg);
    } catch (Exception e) {
        e.printStackTrace();
    }
}



    public static void show(int msg) {
        try {
            if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
                return;
            }
//            SmartToast.show(msg);
            SmartToast
                    .classic()
                    .config()
                    .backgroundColor(Color.parseColor("#99000000"))
                    .msgColorResource(R.color.exo_white)
                    .msgSize(14)
                    .apply()
                    .showInCenter(msg);
//            SmartToast.original().backgroundColor(Color.parseColor("#99000000")).textColorRes(R.color.white).textSizeSp(14).apply().showInCenter(msg);
//            show(Utils.getApp().getString(id));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void showCustom(Context context, int iconId, String msg) {
        try {
            if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
                return;
            }
            SmartToast.show(msg);
            /*SmartToast toastUI = new SmartToast() {
                @Override
                protected View createUI(CharSequence msg, UIArguments uiArguments) {
                    //取出参数值并设置
                    Object argValue = uiArguments.getArg(UIArguments.ARGUMENT_ICON);
                    View inflate = View.inflate(context, R.layout.new_toast_bg_layout, null);
                    ImageView imageView = inflate.findViewById(R.id.iv_icon);
                    TextView textView = inflate.findViewById(R.id.tv_message);
                    imageView.setImageResource((int) argValue);
                    textView.setText(msg);
                    return inflate;
                }
            };
            SmartToast.toastUI(toastUI).addArg(UIArguments.ARGUMENT_ICON, iconId).apply().showInCenter(msg);*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void showCustom(Context context, int iconId, int msg) {
        try {
            if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
                return;
            }
            SmartToast.show(msg);
            /*ToastUI toastUI = new ToastUI() {
                @Override
                protected View createUI(CharSequence msg, UIArguments uiArguments) {
                    //取出参数值并设置
                    Object argValue = uiArguments.getArg(UIArguments.ARGUMENT_ICON);
                    View inflate = View.inflate(context, R.layout.new_toast_bg_layout, null);
                    ImageView imageView = inflate.findViewById(R.id.iv_icon);
                    TextView textView = inflate.findViewById(R.id.tv_message);
                    imageView.setImageResource((int) argValue);
                    textView.setText(msg);
                    return inflate;
                }
            };
            SmartToast.toastUI(toastUI).addArg(UIArguments.ARGUMENT_ICON, iconId).apply().showInCenter(msg);*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
