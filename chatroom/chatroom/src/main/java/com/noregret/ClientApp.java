package com.noregret;

import com.noregret.Client.Client;

public class ClientApp {
    public static void main(String[] args) {
        Client client = new Client();
        client.init("127.0.0.1", 8080);
    }
}
