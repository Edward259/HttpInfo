package com.onyx.httpinfo.bean;

import org.json.JSONException;
import org.json.JSONObject;

import fairy.easy.httpmodel.resource.ping.PingBean;

/**
 * Created by Edward.
 * Date: 2020/1/3
 * Time: 19:17
 * Desc:
 */
public class PingResultBean {
    private String url;
    private int sendCount;
    private int receiverTime;
    private int lossTime;
    private boolean isReceiver;
    private String ip;
    //rtt round trip time
    private float rttAvg;
    private float rttMax;
    private float rttMin;
    private float allTime;

    public PingResultBean(String url, boolean isReceiver, float rttAvg, float rttMax, float rttMin) {
        this.url = url;
        this.isReceiver = isReceiver;
        this.rttAvg = rttAvg;
        this.rttMax = rttMax;
        this.rttMin = rttMin;
    }

    public PingResultBean(String url, int receiverTime, int lossTime, float rttAvg, float rttMax, float rttMin, String ip) {
        this.url = url;
        this.receiverTime = receiverTime;
        this.lossTime = lossTime;
        this.sendCount = receiverTime + lossTime;
        this.rttAvg = rttAvg;
        this.rttMax = rttMax;
        this.rttMin = rttMin;
        this.ip = ip;
    }

    public PingResultBean(String url) {
        this.url = url;
    }

    public boolean isReceiver() {
        return isReceiver;
    }

    public void setReceiver(boolean receiver) {
        isReceiver = receiver;
    }

    public float getAllTime() {
        return allTime;
    }

    public void setAllTime(float allTime) {
        this.allTime = allTime;
        if (receiverTime > 0) {
            setRttAvg(allTime / receiverTime);
        }
    }
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getSendCount() {
        return sendCount;
    }

    public void setSendCount(int sendCount) {
        this.sendCount = sendCount;
    }

    public int getReceiverTime() {
        return receiverTime;
    }

    public void setReceiverTime(int receiverTime) {
        this.receiverTime = receiverTime;
    }

    public int getLossTime() {
        return lossTime;
    }

    public void setLossTime(int lossTime) {
        this.lossTime = lossTime;
    }

    public float getRttAvg() {
        return rttAvg;
    }

    public void setRttAvg(float rttAvg) {
        this.rttAvg = rttAvg;
    }

    public float getRttMax() {
        return rttMax;
    }

    public void setRttMax(float rttMax) {
        this.rttMax = rttMax;
    }

    public float getRttMin() {
        return rttMin;
    }

    public void setRttMin(float rttMin) {
        this.rttMin = rttMin;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void increaseSendCount() {
        sendCount++;
    }

    public void increaseLossTime() {
        lossTime++;
    }

    public void increaseReceiverTime(int count) {
        receiverTime += count;
    }

    public void increaseSendCount(int count) {
        sendCount += count;
    }

    public void increaseLossTime(int count) {
        lossTime += count;
    }

    public void increaseReceiverTime() {
        receiverTime++;
    }

    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("url", url);
            jsonObject.put("sendCount", sendCount);
            jsonObject.put("receiverTime", receiverTime);
            jsonObject.put("lossTime", lossTime);
            jsonObject.put("rttAvg", rttAvg);
            jsonObject.put("rttMax", rttMax);
            jsonObject.put("rttMin", rttMin);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    @Override
    public String toString() {
        return PingBean.PingData.ADDRESS_CN + ":" + url + "\n" +
                PingBean.PingData.IP_CN + ":" + ip + "\n" +
                PingBean.PingData.TRANSMITTED_CN + ":" + sendCount + "\n" +
                PingBean.PingData.RECEIVE_CN + ":" + receiverTime + "\n" +
                PingBean.PingData.LOSSRATE_CN + ":" + (sendCount <= 0 ? 0 : (float)lossTime * 100/ sendCount)+ "%" + "\n" +
                PingBean.PingData.RTTMIN_CN + ":" + rttMin + "ms" + "\n" +
                PingBean.PingData.RTTAVG_CN + ":" + rttAvg + "ms" + "\n" +
                PingBean.PingData.RTTMAX_CN + ":" + rttMax + "ms" + "\n" +
                PingBean.PingData.ALLTIME_CN + ":" + allTime + "ms" + "\n";
    }

    public static class PingData {
        public static final String IP = "ip";
        public static final String IP_CN = "IP地址";
        public static final String ADDRESS = "address";
        public static final String ADDRESS_CN = "网址";
        public static final String TTL = "ttl";
        public static final String TTL_CN = "生存时间";
        public static final String TRANSMITTED = "transmitted";
        public static final String TRANSMITTED_CN = "发送包";
        public static final String RECEIVE = "receive";
        public static final String RECEIVE_CN = "接收包";
        public static final String LOSSRATE = "lossRate";
        public static final String LOSSRATE_CN = "丢包率";
        public static final String RTTMIN = "rttMin";
        public static final String RTTMIN_CN = "最小RTT";
        public static final String RTTAVG = "rttAvg";
        public static final String RTTAVG_CN = "平均RTT";
        public static final String RTTMAX = "rttMax";
        public static final String RTTMAX_CN = "最大RTT";
        public static final String RTTMDEV = "rttMDev";
        public static final String RTTMDEV_CN = "算术平均偏差RTT";
        public static final String ERROR = "status";
        public static final String ERROR_CN = "执行结果";
        public static final String ALLTIME = "allTime";
        public static final String ALLTIME_CN = "总消耗时间";
    }
}
