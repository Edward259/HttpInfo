package com.onyx.httpinfo;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.onyx.httpinfo.bean.PingResultBean;
import com.onyx.httpinfo.widget.LoadingDialog;
import com.onyx.httpinfo.widget.ResultDialog;

import org.json.JSONArray;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public static String[] pingUrls = new String[]{"61.135.169.121", "https://www.qq.com", "https://www.163.com", "https://www.sohu.com", "http://log.onyx-international.cn", "http://ip.luojilab.com"};
    public static String RESULT_PATH = Environment.getExternalStorageDirectory() + File.separator + "Download";
    public String fileSavePath = "";
    private List<PingResultBean> pingResults = new ArrayList<>();
    private Map<String, PingResultBean> resultMap = new HashMap<>();
    private ExecutorService executorService;
    private Intent feedbackService;
    private BroadcastReceiver feedbackReceiver;
    private boolean needDetection = true;

    @SuppressLint("InvalidWakeLockTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        setTitle("评测结果");
        textView = findViewById(R.id.status_tv);
        initData();
        if (!NetWork.isNetworkAvailable(ResultActivity.this)) {
            Toast.makeText(ResultActivity.this, "NETWORK_ERROR", Toast.LENGTH_LONG).show();
            return;
        }
        showDialog();
        acquireWakeLock();
        getNetStatusInfo();
        registerReceiver();
    }

    private void initData() {
        for (String pingUrl : pingUrls) {
            resultMap.put(pingUrl, new PingResultBean(pingUrl));
        }
    }

    private void getNetStatusInfo() {
        Log.i(getLocalClassName(), "getNetStatusInfo");
        executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            Log.i(getLocalClassName(), "getNetStatusInfo thread");
            for (int i = 0; i < pingUrls.length; i++) {
                getPing(i);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Log.i(getLocalClassName(), "before getNetStatusInfo thread");
            runOnUiThread(() -> {
                Log.i(getLocalClassName(), "in getNetStatusInfo thread");
                if (!ResultActivity.this.isFinishing() && needDetection) {
                    getNetStatusInfo();
                }
            });
            Log.i(getLocalClassName(), "getNetStatusInfo thread finished");
        });
        executorService.shutdown();
    }

    private void getPing(int position) {
        Log.i(getLocalClassName(), "getPing");
        Ping ping = new Ping(pingUrls[position]);
        PingBean pingBean = ping.getPingInfo();
        addPingResult(pingBean, pingUrls[position]);
        Log.i(getLocalClassName(), "before getPing thread");
        runOnUiThread(() -> {
            Log.i(getLocalClassName(), "in: getPing thread: " + pingBean);
            if (pingBean != null) {
                PingResultBean pingResultBean = resultMap.get(pingBean.getAddress());
                if (pingResultBean != null && pingResultBean.getSendCount() > 1) {
                    pingBean.setTransmitted(pingResultBean.getSendCount());
                    pingBean.setReceive(pingResultBean.getReceiverTime());
                    pingBean.setLossRate((float) pingResultBean.getLossTime() / pingResultBean.getSendCount() * 100);
                    pingBean.setRttMin(pingResultBean.getRttMin());
                    pingBean.setRttMax(pingResultBean.getRttMax());
                    pingBean.setRttAvg(pingResultBean.getRttAvg());
                    pingBean.setAllTime((int) pingResultBean.getAllTime());
                }
                textView.setText(pingBean.toString());
                Log.i(getLocalClassName(), "after getPing thread");
            }
        });
    }

    private void addPingResult(PingBean pingBean, String url) {
        PingResultBean pingResultBean = new PingResultBean(url, pingBean.getReceive() == 1, pingBean.getRttAvg(), pingBean.getRttMax(), pingBean.getRttMin());
        pingResults.add(pingResultBean);
        if (pingResults.size() > pingUrls.length  && pingResults.size() % pingUrls.length== 0) {
            pingResultStatistics();
        }
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
            loadingDialog.setText("结束评测并发送错误反馈");
            loadingDialog.setCanceledOnTouchOutside(false);
            loadingDialog.setOnConfirmListener(new LoadingDialog.onConfirmClickListener() {
                @Override
                public void onConfirm() {
                    sendFeedback();
                    loadingDialog.setText("反馈中...");
                    needDetection = false;
                }
            });
        }
        if (!this.isFinishing()) {
            loadingDialog.show();
        }
    }

    private void sendFeedback() {
        saveToLocal();
        feedbackService = new Intent();
        feedbackService.setComponent(new ComponentName(Constants.FEEDBACK_PACKET_NAME, Constants.FEEDBACK_SERVICE_NAME));
        feedbackService.putExtra(Constants.FEEDBACK_TITLE, "Network Detection");
        feedbackService.putExtra(Constants.FEEDBACK_DES, pingResultStatistics());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(feedbackService);
        } else {
            startService(feedbackService);
        }
    }

    private boolean stopService() {
        if (feedbackService == null) {
            return false;
        }
        return stopService(feedbackService);
    }

    private void registerReceiver() {
        feedbackReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean succeed = (boolean) intent.getExtras().get(Constants.FEEDBACK_STATUS_KEY);
                if (!isFinishing()) {
                    ResultDialog dialog = new ResultDialog(ResultActivity.this);
                    if (succeed) {
                        dialog.setResult("反馈成功！");
                        dialog.setOnConfirmListener(new ResultDialog.onConfirmClickListener() {
                            @Override
                            public void onConfirm() {
                                finish();
                            }
                        });
                    } else {
                        dialog.setResult("反馈失败，请重试");
                        dialog.setOnConfirmListener(new ResultDialog.onConfirmClickListener() {
                            @Override
                            public void onConfirm() {
                                dialog.dismiss();
                            }
                        });
                    }
                    dialog.setPath("结果保存路径：" + fileSavePath);
                    dialog.show();
                }
                disDialog();
                if (feedbackService != null) {
                    stopService();
                }
            }
        };
        registerReceiver(feedbackReceiver, new IntentFilter(Constants.FEEDBACK_ACTION));
    }

    private void saveToLocal() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File file = new File(RESULT_PATH, ("HttpInfo-" + new SimpleDateFormat("yyyy-MM-dd HH-mm").format(new Date()) + "." + "txt"));
                fileSavePath = file.getAbsolutePath();
                if (!file.exists()) {
                    file.getParentFile().mkdirs();
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(file);
                    fos.write(getResult().getBytes());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void unregisterReceiver() {
        if (feedbackReceiver != null) {
            unregisterReceiver(feedbackReceiver);
        }
    }

    private String pingResultStatistics() {
        PingResultBean originResult;
        for (PingResultBean result : pingResults) {
            originResult = resultMap.get(result.getUrl());
            originResult.increaseSendCount();
            if (result.isReceiver()) {
                originResult.increaseReceiverTime();
            } else {
                originResult.increaseLossTime();
            }
            if (originResult.getAllTime() > 0) {
                originResult.setAllTime((result.getRttAvg() + originResult.getAllTime()));
            } else {
                originResult.setAllTime(result.getRttAvg());
            }
            if (originResult.getRttMin() > 0 && result.getRttMin() > 0) {
                originResult.setRttMin(Math.min(result.getRttMin(), originResult.getRttMin()));
            } else {
                originResult.setRttMin(Math.max(result.getRttMin(), originResult.getRttMin()));
            }
            originResult.setRttMax(Math.max(result.getRttMax(), originResult.getRttMax()));
        }
        return getResult();
    }

    @NonNull
    private String getResult() {
        JSONArray array = new JSONArray();
        for (String key : resultMap.keySet()) {
            array.put(resultMap.get(key).toJson());
        }
        return array.toString();
    }

    private void disDialog() {
        if (null != loadingDialog) {
            loadingDialog.cancel();
            loadingDialog.dismiss();
            loadingDialog = null;
            needDetection = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disDialog();
        releaseWakeLock();
        stopService();
        unregisterReceiver();
    }
}
