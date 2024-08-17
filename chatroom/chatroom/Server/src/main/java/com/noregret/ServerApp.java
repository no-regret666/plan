package com.noregret;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.util.Scanner;

@SpringBootApplication
public class ServerApp {
    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(ServerApp.class, args);
        Server server = context.getBean(Server.class);
//        System.out.println("请输入端口号:");
//        Scanner scanner = new Scanner(System.in);
//        int port = scanner.nextInt();
        server.init(8080);
    }
}