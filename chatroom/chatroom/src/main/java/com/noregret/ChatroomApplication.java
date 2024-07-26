package com.noregret;

import com.noregret.UI.WelcomeUI;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class ChatroomApplication {
    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(ChatroomApplication.class, args);
        WelcomeUI userUI = (WelcomeUI) context.getBean("welcomeUI");
        userUI.menu();
    }
}
