package com.yuong.demo.wifi;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

/**
 * 获得当前设备的ip
 * Created by yuandong on 2018/11/5.
 */

public class IpUtil {

    /**
     * 获得设备在局域网中的ip
     */
    public static void getIpInLAN() throws SocketException {
        // 获得本机的所有网络接口
        Enumeration<NetworkInterface> nifs = NetworkInterface.getNetworkInterfaces();

        while (nifs.hasMoreElements()) {
            NetworkInterface nif = nifs.nextElement();

            // 获得与该网络接口绑定的 IP 地址，一般只有一个
            Enumeration<InetAddress> addresses = nif.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();

                if (addr instanceof Inet4Address) { // 只关心 IPv4 地址
                    System.out.println("网卡接口名称：" + nif.getName());
                    System.out.println("网卡接口地址：" + addr.getHostAddress());
                    System.out.println();
                }
            }
        }
    }

    public InetAddress getLANAddressOnWindows() {
        try {
            Enumeration<NetworkInterface> nifs = NetworkInterface.getNetworkInterfaces();
            while (nifs.hasMoreElements()) {
                NetworkInterface nif = nifs.nextElement();

                if (nif.getName().startsWith("wlan")) {
                    Enumeration<InetAddress> addresses = nif.getInetAddresses();

                    while (addresses.hasMoreElements()) {

                        InetAddress addr = addresses.nextElement();
                        System.out.println("网卡接口名称：" + nif.getName());
                        if (addr.getAddress().length == 4) { // 速度快于 instanceof
                            return addr;
                        }
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace(System.err);
        }
        return null;
    }


    /**
     * 获得设备在局域网中的ip
     */
    public static void getIpInLAN2() throws SocketException {
        // 获得本机的所有网络接口
        Enumeration<NetworkInterface> nifs = NetworkInterface.getNetworkInterfaces();

        while (nifs.hasMoreElements()) {
            NetworkInterface intf = nifs.nextElement();


            // 获得与该网络接口绑定的 IP 地址，一般只有一个
            List<InterfaceAddress> mList = intf.getInterfaceAddresses();
            for (InterfaceAddress l : mList) {
                InetAddress inetAddress = l.getAddress();
                if (!inetAddress.isLoopbackAddress()) {
                    String hostAddress = inetAddress.getHostAddress();
                    System.out.println("---------------------> "+hostAddress);
                    if (hostAddress.indexOf(":") > 0) {
                        continue;
                    } else {
                        //ipAddress = hostAddress;
                        //maskAddress = calcMaskByPrefixLength(l.getNetworkPrefixLength());
                    }
                }
            }
        }
    }

}
