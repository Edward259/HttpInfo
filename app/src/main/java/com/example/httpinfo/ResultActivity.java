package com.example.httpinfo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import com.example.httpinfo.widget.LoadingDialog;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fairy.easy.httpmodel.resource.ping.PingBean;
import fairy.easy.httpmodel.util.NetWork;
import fairy.easy.httpmodel.util.Ping;


public class ResultActivity extends AppCompatActivity {

    private int position = 0;
    private LoadingDialog loadingDialog;
    private TextView textView;
    private PowerManager.WakeLock wakeLock = null;
    public static String[] pingUrls = new String[]{"61.135.169.121", "https://www.qq.com",
            "https://www.163.com", "https://www.sohu.com"};
    private ExecutorService executorService;


    @SuppressLint("InvalidWakeLockTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        setTitle("评测结果");
        textView = findViewById(R.id.status_tv);
        if (!NetWork.isNetworkAvailable(ResultActivity.this)) {
            Toast.makeText(ResultActivity.this, "NETWORK_ERROR", Toast.LENGTH_LONG).show();
            return;
        }
        showDialog();
        acquireWakeLock();
        getNetStatusInfo();
    }

    private void getNetStatusInfo() {
        executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            for (int i = 0; i < pingUrls.length; i++) {
                position = i;
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                getPing(position);
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
            runOnUiThread(() -> {
                if (!ResultActivity.this.isFinishing()) {
                    getNetStatusInfo();
                }
            });
        });
        executorService.shutdown();
    }

    private void getPing(int position) {
        Ping ping = new Ping(pingUrls[position]);
        PingBean pingBean = ping.getPingInfo();
        runOnUiThread(() -> {
            if (pingBean != null) {
                textView.setText(pingBean.toString());
            }
        });
    }

    @SuppressLint("InvalidWakeLockTag")
    private void acquireWakeLock() {
        if (null == wakeLock) {
            PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK,
                    "PostLocationService");
        }
        if (null != wakeLock) {
            wakeLock.acquire();
        }
    }

    private void releaseWakeLock() {
        if (null != wakeLock) {
            wakeLock.release();
            wakeLock = null;
        }
    }

    private void showDialog() {
        if (null == loadingDialog) {
            loadingDialog = new LoadingDialog(this);
            loadingDialog.setText("正在检测..");
            loadingDialog.setCancelable(true);
        }
        if (!this.isFinishing()) {
            loadingDialog.show();
        }
    }

    private void disDialog() {
        if (null != loadingDialog) {
            loadingDialog.cancel();
            loadingDialog.dismiss();
            loadingDialog = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disDialog();
        releaseWakeLock();
    }
}
