package com.glasses.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public final class LanIpUtil {

    private LanIpUtil() {
    }

    public static String findLanIp() {
        try {
            List<InetAddress> candidates = new ArrayList<>();
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (ni.isLoopback() || !ni.isUp() || ni.isVirtual()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        candidates.add(addr);
                    }
                }
            }
            for (InetAddress addr : candidates) {
                if (addr.getHostAddress().startsWith("192.168.")) {
                    return addr.getHostAddress();
                }
            }
            for (InetAddress addr : candidates) {
                if (addr.getHostAddress().startsWith("10.")) {
                    return addr.getHostAddress();
                }
            }
            for (InetAddress addr : candidates) {
                String h = addr.getHostAddress();
                if (h.startsWith("172.")) {
                    int second = Integer.parseInt(h.split("\\.")[1]);
                    if (second >= 16 && second <= 31) {
                        return h;
                    }
                }
            }
            if (!candidates.isEmpty()) {
                return candidates.get(0).getHostAddress();
            }
        } catch (SocketException e) {
            // ignore
        }
        return null;
    }
}
