package fairy.easy.httpmodel.resource.net;

import android.content.Context;

import fairy.easy.httpmodel.HttpModelHelper;
import fairy.easy.httpmodel.resource.HttpType;
import fairy.easy.httpmodel.resource.Input;
import fairy.easy.httpmodel.util.Dns;
import fairy.easy.httpmodel.util.HttpLog;
import fairy.easy.httpmodel.util.LogTime;
import fairy.easy.httpmodel.util.Net;

import static fairy.easy.httpmodel.util.NetWork.isNetworkAvailable;

public class NetHelper {

    public static void getNetParam()  {
        long startTime = LogTime.getLogTime();
        final NetBean netBean = new NetBean();
        HttpLog.i("Net is end14");
        Context context = HttpModelHelper.getInstance().getContext();
        HttpLog.i("Net is end13");
        netBean.setNetworkAvailable(isNetworkAvailable(context));
        HttpLog.i("Net is end12");
        netBean.setNetWorkType(Net.networkType(context));
        HttpLog.i("Net is end11");
        netBean.setMobileType(Net.networkTypeMobile(context));
        HttpLog.i("Net is end10");
        netBean.setWifiRssi(Net.getWifiRssi(context));
        HttpLog.i("Net is end9");
        netBean.setWifiLevel(Net.calculateSignalLevel(netBean.getWifiRssi()));
        HttpLog.i("Net is end8");
        netBean.setWifiLevelValue(Net.checkSignalRssi(netBean.getWifiLevel()));
        HttpLog.i("Net is end7");
        netBean.setIp(Net.getClientIp());
        HttpLog.i("Net is end6");
        netBean.setDns(Dns.readDnsServers(context).length>0?Dns.readDnsServers(context)[0]:"*");
        HttpLog.i("Net is end5");
        Net.getOutPutDns(netBean);
        HttpLog.i("Net is end4");
        netBean.setRoaming(Net.checkIsRoaming(context));
        HttpLog.i("Net is end3");
        Net.getMobileDbm(context, netBean);
        HttpLog.i("Net is end2");
        netBean.setMobLevelValue(Net.checkSignalRssi(netBean.getMobLevel()));
        HttpLog.i("Net is end1");
        netBean.setTotalName(LogTime.getElapsedMillis(startTime));
        HttpLog.i("Net is end");
        Input.onSuccess(HttpType.NET, netBean.toJSONObject());
    }


}
