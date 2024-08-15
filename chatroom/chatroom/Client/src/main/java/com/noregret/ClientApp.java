package com.noregret;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ClientApp {
    public static void main(String[] args) throws UnknownHostException {
        String ip = Utils.getIP();
        Client client = new Client();
        client.init(ip, 8080);
    }
}