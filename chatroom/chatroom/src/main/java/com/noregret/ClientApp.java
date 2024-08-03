package com.noregret;

import com.noregret.Client.Client;

public class ClientApp {
    public static void main(String[] args) {
        Client client = new Client();
        client.init("10.30.0.150", 8080);
    }
}
