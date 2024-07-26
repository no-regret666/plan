package com.noregret.UI;

import com.noregret.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.noregret.pojo.User;

import java.util.Scanner;

@Component
public class WelcomeUI {
    @Autowired
    private UserService userService;

    public void menu() {
        System.out.println("-----------------------------------------");
        System.out.println("              欢迎进入聊天室                ");
        System.out.println("-----------------------------------------");
        System.out.println("           a.登录       b.注册             ");
        System.out.println("          c.找回密码     d.注销             ");
        System.out.println("                 e.退出                    ");
        System.out.println("------------------------------------------");
        Scanner sc = new Scanner(System.in);
        char c = sc.next().charAt(0);
        switch (c) {
            case 'a':
                login();
                break;
            case 'b':
                register();
                break;
            case 'c':
                find();
                break;
            case 'd':
                unregister();
                break;
            case 'e':
                System.exit(0);
        }
    }

    public void login() {
        Scanner sc = new Scanner(System.in);
        System.out.println("请输入用户名:");
        String username = sc.nextLine();
        System.out.println("请输入密码:");
        String password = sc.nextLine();
        User user = userService.getUserByUsername(username);
        if (user == null) {
            System.out.println("该用户不存在！");
        } else {
            if (user.getPassword().equals(password)) {
                System.out.println("登录成功！");
                UserUI.menu(username);
            } else {
                System.out.println("密码错误！");
            }
        }
    }

    public void register() {
        Scanner sc = new Scanner(System.in);
        System.out.println("请输入用户名:");
        String username = sc.nextLine();
        User user = userService.getUserByUsername(username);
        while (user != null) {
            System.out.println("该用户名已存在！");
            user = userService.getUserByUsername(sc.nextLine());
        }
        System.out.println("请输入密码:");
        String password = sc.nextLine();
        userService.insertUser(username, password);
    }

    public void find() {
        Scanner sc = new Scanner(System.in);
        System.out.println("请输入用户名:");
        String username = sc.nextLine();
        User user = userService.getUserByUsername(username);
        System.out.println("您的密码为" + user.getUsername());
    }

    public void unregister() {
        Scanner sc = new Scanner(System.in);
        System.out.println("请输入用户名:");
        String username = sc.nextLine();
        userService.deleteUser(username);
    }
}
