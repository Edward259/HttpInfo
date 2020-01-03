package com.onyx.httpinfo.widget;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.onyx.httpinfo.R;

public class LoadingDialog extends Dialog {

    /**
     * 提示文字
     */
    private TextView mTextView;
    private onConfirmClickListener listener;

    public interface onConfirmClickListener {
        void onConfirm();
    }

    public LoadingDialog(Context context) {
        super(context, R.style.WinDialog);
        setContentView(R.layout.dialog_loading);
        mTextView = (TextView) findViewById(android.R.id.message);
        mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onConfirm();
                }
            }
        });
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

    public void setOnConfirmListener(onConfirmClickListener listener) {
        this.listener = listener;
    }
}
