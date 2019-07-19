package org.study.service.timer.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.study.common.util.utils.StringUtil;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class IPUtil {
    private static Logger logger = LoggerFactory.getLogger(IPUtil.class);

    public static String getFirstLocalIp() {
        String firstLocalIp = null;
        try {
            Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
            while (enumeration.hasMoreElements()) {
                NetworkInterface iface = enumeration.nextElement();
                // filters out 127.0.0.1 and inactive interfaces
                if (iface.isLoopback() || !iface.isUp()){
                    continue;
                }

                Enumeration<InetAddress> inetAddresses = iface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    String ip = inetAddresses.nextElement().getHostAddress();
                    // 排除 回环IP/ipv6 地址
                    if (ip.contains(":") || StringUtil.isEmpty(ip)){
                        continue;
                    }

                    firstLocalIp = ip;
                    break;
                }
            }
        } catch (SocketException e) {
            logger.error("获取本地IP时出现异常", e);
        }
        return firstLocalIp;
    }
}
