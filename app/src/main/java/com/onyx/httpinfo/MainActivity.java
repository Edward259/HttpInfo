package com.onyx.httpinfo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

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
        //获取手机强度使用
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }

        infoTv = findViewById(R.id.main_activity_tv_info);
        wifiManager = ((WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE));
        dhcpInfo = wifiManager.getDhcpInfo();
        wifiInfo = wifiManager.getConnectionInfo();
        findViewById(R.id.main_activity_start_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
                startActivity(intent);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();
        StringBuilder sb = new StringBuilder();
        sb.append("网络信息：");
        sb.append("\nipAddress：" + intToIp(dhcpInfo.ipAddress));
        sb.append("\nnetmask：" + getNetMask());
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
        sb.append("Wifi信息：");
        sb.append("\nIpAddress：" + intToIp(wifiInfo.getIpAddress()));
        sb.append("\nMacAddress：" + wifiInfo.getMacAddress());
        infoTv.setText(sb.toString());
    }
    private String intToIp(int paramInt) {
        return (paramInt & 0xFF) + "." + (0xFF & paramInt >> 8) + "." + (0xFF & paramInt >> 16) + "."
                + (0xFF & paramInt >> 24);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private String getNetMask() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network network = (connectivityManager).getActiveNetwork();
        LinkProperties linkProperties = connectivityManager.getLinkProperties(network);
        List<LinkAddress> linkAddressList = linkProperties.getLinkAddresses();
        LinkAddress linkAddress = linkAddressList.get(0);
        String ipAddress = linkAddress.getAddress().getHostAddress();
        String[] parts = ipAddress.split("/");
        String ip = parts[0];
        int prefix;
        if (parts.length < 2) {
            prefix = 0;
        } else {
            prefix = Integer.parseInt(parts[1]);
        }
        int mask = 0xffffffff << (32 - prefix);
        System.out.println("Prefix=" + prefix);
        System.out.println("Address=" + ip);

        int value = mask;
        byte[] bytes = new byte[]{
                (byte) (value >>> 24), (byte) (value >> 16 & 0xff), (byte) (value >> 8 & 0xff), (byte) (value & 0xff)};

        InetAddress netAddr = null;
        try {
            netAddr = InetAddress.getByAddress(bytes);
            System.out.println("Mask=" + netAddr.getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return "";
    }

}
