package com.onyx.httpinfo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.onyx.httpinfo.utils.NetworkInfoUtils;

public class MainActivity extends AppCompatActivity {

    private WifiManager wifiManager;
    private WifiInfo wifiInfo;
    private DhcpInfo dhcpInfo;
    private TextView infoTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("网络诊断");
        requestPermissions();
        initData();
        initView();
    }

    private void requestPermissions() {
        //获取手机强度使用
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
    }

    private void initData() {
        wifiManager = ((WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE));
        dhcpInfo = wifiManager.getDhcpInfo();
        wifiInfo = wifiManager.getConnectionInfo();
    }

    private void initView() {
        infoTv = findViewById(R.id.main_activity_tv_info);
        findViewById(R.id.main_activity_start_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!NetworkInfoUtils.isWifiConnect(MainActivity.this)) {
                    Toast.makeText(MainActivity.this, "网络未连接，请检查后重试！", Toast.LENGTH_LONG).show();
                    return;
                }
                Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
                startActivity(intent);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();
        loadNetworkInfo();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void loadNetworkInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("网络信息：");
        sb.append("\nipAddress：" + intToIp(dhcpInfo.ipAddress));
        sb.append("\nnetmask：" + NetworkInfoUtils.getNetMask(this));
        sb.append("\ngateway：" + intToIp(dhcpInfo.gateway));
        sb.append("\nserverAddress：" + intToIp(dhcpInfo.serverAddress));
        sb.append("\ndns1：" + intToIp(dhcpInfo.dns1));
        sb.append("\ndns2：" + intToIp(dhcpInfo.dns2));
        sb.append("\n");
        System.out.println(intToIp(dhcpInfo.ipAddress));
        System.out.println(intToIp(dhcpInfo.netmask));
        System.out.println(intToIp(dhcpInfo.gateway));
        System.out.println(intToIp(dhcpInfo.serverAddress));
        System.out.println(intToIp(dhcpInfo.dns1));
        System.out.println(intToIp(dhcpInfo.dns2));
        System.out.println(dhcpInfo.leaseDuration);
        sb.append("Wifi信息：" + (!NetworkInfoUtils.isWifiConnect(this) ? "未连接" : wifiInfo.getSSID().substring(1, wifiInfo.getSSID().length() - 1)));
        sb.append("\nIpAddress：" + intToIp(wifiInfo.getIpAddress()));
        sb.append("\nMacAddress：" + NetworkInfoUtils.getMacAddress());
        infoTv.setText(sb.toString());
    }

    private String intToIp(int paramInt) {
        return (paramInt & 0xFF) + "." + (0xFF & paramInt >> 8) + "." + (0xFF & paramInt >> 16) + "."
                + (0xFF & paramInt >> 24);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.exit(0);
    }
}
