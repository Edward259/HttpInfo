package com.example.httpinfo.utils;

import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Context;
import android.support.annotation.StringRes;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;


/**
 * @author Kaiguang
 * @Description
 * @Time 2018/9/13
 */
public class DialogUtils {

    private static final float COMMON_DIALOG_WIDTH_THRESHOLD = 0.67f;

    public static void applyCommonSize(Dialog dialog) {
        if (dialog == null || dialog.getWindow() == null) {
            return;
        }
        dialog.getWindow().setLayout(getDefaultDialogWidth(dialog.getContext()),
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public static int getDefaultDialogWidth(Context context) {
        float width = 100f;
        return (int) (width * COMMON_DIALOG_WIDTH_THRESHOLD);
    }

}
