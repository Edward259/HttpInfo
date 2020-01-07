package com.onyx.httpinfo.bean;

import org.json.JSONException;
import org.json.JSONObject;

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

    public void increaseSendCount() {
        sendCount++;
    }

    public void increaseLossTime() {
        lossTime++;
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
        return "{" +
                "\"url\":" +"\"" +  url + "\"" +
                ", \"sendCount\":" + sendCount +
                ", \"receiverTime\":" + receiverTime +
                ", \"lossTime\":" + lossTime +
                ", \"rttAvg\":" + rttAvg +
                ", \"rttMax\":" + rttMax +
                ", \"rttMin\":" + rttMin +
                '}';
    }
}
