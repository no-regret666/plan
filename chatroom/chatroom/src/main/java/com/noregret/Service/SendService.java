package com.noregret.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.noregret.MsgType;
import io.netty.channel.Channel;
import java.util.Scanner;

public class SendService {
    private Channel channel;
    Scanner sc = new Scanner(System.in);

    public SendService(Channel channel) {
        this.channel = channel;
    }

    public void sendMsg() {
        menu();
        while(true) {
            char c = sc.next().charAt(0);
            switch (c) {
                case 'a':
                    login();
                    break;
                case 'b':

            }
        }
    }

    public void menu() {
        System.out.println("-----------------------------------------");
        System.out.println("              欢迎进入聊天室                ");
        System.out.println("-----------------------------------------");
        System.out.println("           a.登录       b.注册             ");
        System.out.println("          c.找回密码     d.注销             ");
        System.out.println("                 e.退出                    ");
        System.out.println("------------------------------------------");
    }

    public void login() {
        System.out.println("请输入用户名:");
        String username = sc.nextLine();
        System.out.println("请输入密码:");
        String password = sc.nextLine();

        //封装json数据
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode node = objectMapper.createObjectNode();
        node.put("username", username);
        node.put("password", password);
        node.put("type",String.valueOf(MsgType.MSG_LOGIN));
        String msg = node.toString();

        //发送至服务器
        channel.writeAndFlush(msg);
    }
}
