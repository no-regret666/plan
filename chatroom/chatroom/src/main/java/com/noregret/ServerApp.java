package com.noregret;

import com.noregret.Server.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class ServerApp {
    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(ServerApp.class, args);
        Server server = context.getBean(Server.class);
        server.init(8080);
    }
}