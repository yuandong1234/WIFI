package com.yuong.demo.wifi;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by yuandong on 2018/10/22.
 */

public class WifiUtil {
    private static final String TAG = WifiUtil.class.getSimpleName();

    //    public static final String[] strings = {"NONE", "WPA_PSK", "WPA_EAP", "IEEE8021X",
//            "WPA2_PSK", "OSEN", "FT_PSK", "FT_EAP"};//目前的加密方案
    public static final int SECURITY_NONE = 1;//不加密
    public static final int SECURITY_WEP = 2;//WEP
    public static final int SECURITY_WPA = 3;//WPA

    /**
     * 获得WiFi热点加密方案
     *
     * @param capabilities WifiConfiguration
     */
    //WifiConfiguration
    private static List<String> getEncryptionSchemes(String capabilities) {

        if (TextUtils.isEmpty(capabilities)) return null;

        capabilities = capabilities.replaceAll("\\[", "");

        String[] array = capabilities.split("\\]");
        return Arrays.asList(array);
    }

    /**
     * WiFi ap 是否加密
     *
     * @param capabilities
     * @return
     */
    public static boolean isEncrypted(String capabilities) {
        List<String> schemes = getEncryptionSchemes(capabilities);
        if (schemes == null || schemes.size() == 0) return false;

        List<String> list = new ArrayList<>(schemes);
        list.remove("ESS");
        list.remove("WPS");
        return list.size() > 0;
    }


    public static int getEncryptedType(String capabilities) {
        List<String> schemes = getEncryptionSchemes(capabilities);
        if (schemes == null || schemes.size() == 0) return 0;

        for (String temp : schemes) {
            String scheme = temp.toUpperCase();
            if (scheme.contains("WPA")) {
                return SECURITY_WPA;
            } else if (scheme.contains("WEP")) {
                return SECURITY_WEP;
            } else {//其他的加密类型待添加

            }
        }
        return SECURITY_NONE;
    }

    /**
     * 忘记某一个wifi密码
     *
     * @param wifiManager
     * @param targetSsid
     */
    public static void removeWifiBySsid(WifiManager wifiManager, String targetSsid) {
        Log.d(TAG, "try to removeWifiBySsid, targetSsid=" + targetSsid);
        List<WifiConfiguration> wifiConfigs = wifiManager.getConfiguredNetworks();

        for (WifiConfiguration wifiConfig : wifiConfigs) {
            String ssid = wifiConfig.SSID;
            Log.d(TAG, "removeWifiBySsid ssid=" + ssid);
            if (ssid.equals(targetSsid)) {
                Log.d(TAG, "removeWifiBySsid success, SSID = " + wifiConfig.SSID + " netId = " + String.valueOf(wifiConfig.networkId));
                wifiManager.removeNetwork(wifiConfig.networkId);
                wifiManager.saveConfiguration();
            }
        }
    }

    public static String getWifiIP(Context context) {
        String ip = null;
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null && wifiManager.isWifiEnabled()) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int i = wifiInfo.getIpAddress();
            ip = (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + (i >> 24 & 0xFF);
        }
        return ip;
    }

    /**
     * wifi获取 已连接网络路由  路由ip地址
     *
     * @param context
     * @return
     */
    public static String getWifiRouteIPAddress(Context context) {
        String ip = null;
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
            int i = dhcpInfo.serverAddress;
            ip = (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + ((i >> 24) & 0xFF);
        }
        return ip;
    }


//    public static String getIp(Context mContext) throws SocketException {
//        String ip = "";
//        //获取wifi服务
//        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
//        if (wifiManager.isWifiEnabled()) {
//            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
//            int ipAddress = wifiInfo.getIpAddress();
//            DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
//            String dhcpInfos = intToIp(dhcpInfo.netmask);
//            String[] split = intToIp(ipAddress).split("\\.");
//            ip = split[0] + "." + split[1] + "." + split[2] + "." + (255 - Integer.parseInt(dhcpInfos.split("\\.")[3]));//根据子网掩码获取广播的IP地址
//        } else {
//            String asd = getInfo();
//            String[] split = asd.split(",");
//            String ipStr = split[0];
//            String NetMask = split[1];
//            String[] split1 = ipStr.split("\\.");
//            ip = split1[0] + "." + split1[1] + "." + split1[2] + "." + (255 - Integer.parseInt(NetMask.split("\\.")[3]));//根据子网掩码获取广播的IP地址
//        }
//        return ip;
//    }



    /**
     * 将ip的整数形式转换成ip形式
     *
     * @param ipInt
     * @return
     */
    public static String int2ip(int ipInt) {
        StringBuilder sb = new StringBuilder();
        sb.append(ipInt & 0xFF).append(".");
        sb.append((ipInt >> 8) & 0xFF).append(".");
        sb.append((ipInt >> 16) & 0xFF).append(".");
        sb.append((ipInt >> 24) & 0xFF);
        return sb.toString();
    }

}
