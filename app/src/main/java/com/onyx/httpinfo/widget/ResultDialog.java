package com.onyx.httpinfo.widget;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.onyx.httpinfo.R;

/**
 * Created by Edward.
 * Date: 2020/1/7
 * Time: 12:27
 * Desc:
 */
public class ResultDialog extends Dialog {
    private TextView result;
    private TextView path;
    private ResultDialog.onConfirmClickListener listener;

    public interface onConfirmClickListener {
        void onConfirm();
    }

    public ResultDialog(Context context) {
        super(context);
        setContentView(R.layout.dialog_result);
        result = (TextView) findViewById(R.id.result);
        path = (TextView) findViewById(R.id.result_data_path);
        TextView confirm = (TextView) findViewById(R.id.confirm);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onConfirm();
                }
            }
        });
    }

    public void setResult(String s) {
        if (result != null) {
            result.setText(s);
            result.setVisibility(View.VISIBLE);
        }
    }

    public void setPath(String s) {
        if (path != null) {
            path.setText(s);
            path.setVisibility(View.VISIBLE);
        }
    }

    public void setOnConfirmListener(ResultDialog.onConfirmClickListener listener) {
        this.listener = listener;
    }

}
