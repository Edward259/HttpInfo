package com.onyx.httpinfo.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.ViewGroup;


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
