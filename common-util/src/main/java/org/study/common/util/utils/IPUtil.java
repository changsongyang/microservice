package org.study.common.util.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class IPUtil {
    private static Logger logger = LoggerFactory.getLogger(IPUtil.class);

    public static String getFirstLocalIp() {
        InetAddress inetIp = getInetAddress();
        return inetIp.getHostAddress();
    }

    public static String getMacAddress(){
        InetAddress inetIp = getInetAddress();
        try {
            StringBuilder sb = new StringBuilder();
            NetworkInterface ni = NetworkInterface.getByInetAddress(inetIp);
            byte[] mac = ni.getHardwareAddress();
            if (mac != null) {
                for (int i = 0; i < mac.length; i++) {
                    sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
                }
            }

            return sb.toString();
        } catch (SocketException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static InetAddress getInetAddress() {
        InetAddress inetIp = null;
        boolean isContinue = true;
        try {
            Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
            while (isContinue && enumeration.hasMoreElements()) {
                NetworkInterface iface = enumeration.nextElement();
                if (iface.isLoopback() || !iface.isUp()){ //filters out 127.0.0.1 and inactive interfaces
                    continue;
                }

                Enumeration<InetAddress> inetAddresses = iface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    inetIp = inetAddresses.nextElement();
                    String ipStr = inetIp.getHostAddress();
                    // 排除 回环IP/ipv6 地址
                    if (StringUtil.isEmpty(ipStr) || ipStr.contains(":")){
                        inetIp = null;
                        continue;
                    }

                    isContinue = false;
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("获取本地IP时出现异常", e);
        }

        if(inetIp == null){
            throw new RuntimeException("获取本地IP失败");
        }

        return inetIp;
    }
}
