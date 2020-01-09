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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.onyx.httpinfo.bean.PingResultBean;
import com.onyx.httpinfo.widget.LoadingDialog;
import com.onyx.httpinfo.widget.ResultDialog;
import com.qiniu.android.netdiag.Output;
import com.qiniu.android.netdiag.Ping;
import com.qiniu.android.netdiag.TcpPing;
import com.qiniu.android.netdiag.TraceRoute;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fairy.easy.httpmodel.util.NetWork;


public class ResultActivity extends AppCompatActivity {

    private int position = 0;
    private LoadingDialog loadingDialog;
    private TextView tvResult;
    private PowerManager.WakeLock wakeLock = null;
    public static String[] pingUrls = new String[]{"61.135.169.121", "www.qq.com", "www.163.com", "www.sohu.com", "log.onyx-international.cn", "ip.luojilab.com"};
    public static String RESULT_PATH = Environment.getExternalStorageDirectory() + File.separator + "Download";
    public String fileSavePath = "";
    private List<PingResultBean> pingResults = new ArrayList<>();
    private List<PingResultBean> tcpPingResults = new ArrayList<>();
    private Map<String, PingResultBean> resultMap = new HashMap<>();
    private Map<String, PingResultBean> tcpResultMap = new HashMap<>();
    private Map<String, String> traceRouteMap = new HashMap<>();
    private Intent feedbackService;
    private BroadcastReceiver feedbackReceiver;
    private boolean needDetection = true;
    private String pingUrl;
    private boolean pingComplete = false;
    private boolean tcpPingComplete = false;
    private boolean traceRouteComplete = false;
    private boolean isFirst = true;
    private boolean showTcp = false;
    private CheckBox showTcpCheckbox;

    @SuppressLint("InvalidWakeLockTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        setTitle("评测结果");
        initView();
        initData();
        if (!NetWork.isNetworkAvailable(ResultActivity.this)) {
            Toast.makeText(ResultActivity.this, "NETWORK_ERROR", Toast.LENGTH_LONG).show();
            return;
        }
        acquireWakeLock();
        startDiagnose();
        showDialog();
        registerReceiver();
    }

    private void initView() {
        tvResult = findViewById(R.id.status_tv);
        showTcpCheckbox = findViewById(R.id.show_tcp);
        showTcpCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String s;
                showTcp = isChecked;
                if (isChecked) {
                    s = resultMap.get(pingUrl).toString();
                } else {
                    s = tcpResultMap.get(pingUrl).toString();
                }
                tvResult.setText(s);
            }
        });
    }

    private void initData() {
        for (String pingUrl : pingUrls) {
            resultMap.put(pingUrl, new PingResultBean(pingUrl));
            tcpResultMap.put(pingUrl, new PingResultBean(pingUrl));
        }
    }

    private void startDiagnose() {
        if (((pingComplete && tcpPingComplete) || isFirst) && needDetection) {
            Log.i(getLocalClassName(), "getPing");
            pingUrl = pingUrls[position];
            if (++position > pingUrls.length - 1) {
                position = 0;
            }
            pingComplete = false;
            tcpPingComplete = false;
            traceRouteComplete = false;
            Ping.start(pingUrl, new Output() {
                @Override
                public void write(String line) {
                    Log.e("TAG", "Ping write: " + line);
                }
            }, new Ping.Callback() {
                @Override
                public void complete(com.qiniu.android.netdiag.Ping.Result r) {
                    pingComplete = true;
                    PingResultBean pingResultBean = new PingResultBean(pingUrl, r.count, r.dropped, r.avg, r.max, r.min, r.ip);
                    pingResults.add(pingResultBean);
                    pingResultStatistics(resultMap, pingResults);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!showTcp) {
                                tvResult.setText(resultMap.get(pingUrl).toString());
                            }
                            startDiagnose();
                        }
                    });
                }
            });
            TcpPing.start(pingUrl, new Output() {
                @Override
                public void write(String line) {
                    Log.e("TAG", "TcpPing write: " + line);
                }
            }, new TcpPing.Callback() {
                @Override
                public void complete(TcpPing.Result r) {
                    tcpPingComplete = true;
                    Log.e("TAG", "TcpPing count: " + r.count + " droped:" + r.dropped);
                    PingResultBean pingResultBean = new PingResultBean(pingUrl, r.count, r.dropped, r.avgTime, r.maxTime, r.minTime, r.ip);
                    tcpPingResults.add(pingResultBean);
                    pingResultStatistics(tcpResultMap, tcpPingResults);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (showTcp) {
                                tvResult.setText(tcpResultMap.get(pingUrl).toString());
                            }
                            startDiagnose();
                        }
                    });
                }
            });
            if (!isFirst) {
                return;
            }
            isFirst = false;
            for (String url : pingUrls) {
                TraceRoute.start(url, new Output() {
                    @Override
                    public void write(String line) {
                        Log.e("TAG", "TraceRoute write: " + line);
                    }
                }, new TraceRoute.Callback() {
                    @Override
                    public void complete(TraceRoute.Result r) {
                        traceRouteComplete = true;
                        traceRouteMap.put(pingUrl, r.content());
                        Log.e("TAG", "TraceRoute ip: " + r.ip + " data:" + r.content());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                startDiagnose();
                            }
                        });
                    }
                });
            }
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
        feedbackService.putExtra(Constants.FEEDBACK_DES, getResultString());
        feedbackService.putExtra(Constants.FEEDBACK_LOCAL_SAVE_PATH, RESULT_PATH + "/HttpInfo-Feedback.zip");
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
                String result = getResultString();
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
                    fos.write(result.getBytes());
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

    @NonNull
    private String getResultString() {
        JSONObject jsonObject = new JSONObject();
        String result = "";
        try {
            jsonObject.put("ping", toJsonArray(resultMap));
            jsonObject.put("tcpPing", toJsonArray(tcpResultMap));
            jsonObject.put("traceRoute", traceRouteToJsonArray(traceRouteMap));
            result = jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    private void unregisterReceiver() {
        if (feedbackReceiver != null) {
            unregisterReceiver(feedbackReceiver);
        }
    }

    private void pingResultStatistics(Map<String, PingResultBean> map, List<PingResultBean> list) {
        PingResultBean originResult;
        for (PingResultBean result : list) {
            originResult = map.get(result.getUrl());
            originResult.setIp(result.getIp());
            originResult.increaseSendCount(result.getSendCount());
            originResult.increaseReceiverTime(result.getReceiverTime());
            originResult.increaseLossTime(result.getLossTime());
            if (originResult.getAllTime() > 0) {
                originResult.setAllTime((result.getRttAvg() * result.getReceiverTime() + originResult.getAllTime()));
            } else {
                originResult.setAllTime(result.getRttAvg() * result.getReceiverTime());
            }
            if (originResult.getRttMin() > 0 && result.getRttMin() > 0) {
                originResult.setRttMin(Math.min(result.getRttMin(), originResult.getRttMin()));
            } else {
                originResult.setRttMin(Math.max(result.getRttMin(), originResult.getRttMin()));
            }
            originResult.setRttMax(Math.max(result.getRttMax(), originResult.getRttMax()));
        }
    }

    @NonNull
    private JSONArray toJsonArray(Map<String, PingResultBean> map) {
        JSONArray array = new JSONArray();
        for (String key : map.keySet()) {
            array.put(map.get(key).toJson());
        }
        return array;
    }

    @NonNull
    private JSONArray traceRouteToJsonArray(Map<String, String> map) {
        JSONArray array = new JSONArray();
        try {
            for (String key : map.keySet()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(key, map.get(key));
                array.put(jsonObject);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return array;
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
