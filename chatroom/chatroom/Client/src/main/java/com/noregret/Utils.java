package com.noregret;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Utils {
    public static String getColoredString(int color, int fontType, String content) {
        return String.format("\033[%d;%dm%s\033[0m", color, fontType, content);
    }

    public static String getIP() throws UnknownHostException {
        InetAddress address = InetAddress.getByName("noregret-arch");
        return address.getHostAddress();
    }
}
