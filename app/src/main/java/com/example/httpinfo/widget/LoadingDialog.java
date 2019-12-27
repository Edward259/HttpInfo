package com.example.httpinfo.widget;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.example.httpinfo.R;

public class LoadingDialog extends Dialog {

    /**
     * 提示文字
     */
    private TextView mTextView;

    public LoadingDialog(Context context) {
        super(context, R.style.WinDialog);
        setContentView(R.layout.dialog_loading);
        mTextView = (TextView) findViewById(android.R.id.message);
    }

    public void setText(String s) {
        if (mTextView != null) {
            mTextView.setText(s);
            mTextView.setVisibility(View.VISIBLE);
        }
    }

    public void setText(int res) {
        if (mTextView != null) {
            mTextView.setText(res);
            mTextView.setVisibility(View.VISIBLE);
        }
    }
}
