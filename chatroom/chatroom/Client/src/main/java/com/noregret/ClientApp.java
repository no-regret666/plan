package com.noregret;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ClientApp {
    public static void main(String[] args) throws UnknownHostException {
        InetAddress address = InetAddress.getByName("noregret-arch");
        String ip = address.getHostAddress();
        Client client = new Client();
        client.init(ip, 8080);
    }
}
