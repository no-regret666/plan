package com.noregret;

import java.net.UnknownHostException;
import java.util.Scanner;

public class ClientApp {
    public static void main(String[] args) throws UnknownHostException {
        String ip = Utils.getIP();
        Client client = new Client();
//        System.out.println("请输入端口号:");
//        Scanner scanner = new Scanner(System.in);
//        int port = scanner.nextInt();
        client.init(ip, 8080);
    }
}