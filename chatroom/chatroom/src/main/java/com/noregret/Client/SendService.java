package com.noregret.Client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.noregret.MsgType;
import io.netty.channel.Channel;

import java.util.List;
import java.util.Scanner;

public class SendService {
    private Channel channel;
    Scanner sc = new Scanner(System.in);

    public SendService(Channel channel) {
        this.channel = channel;
    }

    public void sendMsg() throws InterruptedException, JsonProcessingException {
        menu();
        while (true) {
            char c = sc.nextLine().charAt(0);
            switch (c) {
                case 'a':
                    login();
                    break;
                case 'b':
                    register();
                    break;
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

    public void login() throws InterruptedException, JsonProcessingException {
        System.out.println("请输入用户名:");
        String username = sc.nextLine();
        System.out.println("请输入密码:");
        String password = sc.nextLine();

        //封装json数据
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode node = objectMapper.createObjectNode();
        node.put("username", username);
        node.put("password", password);
        node.put("type", String.valueOf(MsgType.MSG_LOGIN));
        String msg = node.toString();

        //发送至服务器
        channel.writeAndFlush(msg);

        //返回状态码
        int code = ClientHandler.queue.take();
        if (code == 100) {
            System.out.println("登录成功!");
            personHome(username);
        } else if (code == 200) {
            System.out.println("密码错误!");
        } else if (code == 300) {
            System.out.println("该用户不存在!");
        }
    }

    public void register() throws InterruptedException {
        System.out.println("请输入用户名:");
        String username = sc.nextLine();
        System.out.println("请输入密码:");
        String password = sc.nextLine();

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode node = objectMapper.createObjectNode();
        node.put("username", username);
        node.put("password", password);
        node.put("type", String.valueOf(MsgType.MSG_REGISTER));
        String msg = node.toString();

        channel.writeAndFlush(msg);

        int code = ClientHandler.queue.take();
        if (code == 100) {
            System.out.println("注册成功!");
        } else if (code == 200) {
            System.out.println("用户名已存在!");
        }
    }

    public void personHome(String username) throws InterruptedException, JsonProcessingException {
        System.out.println("-------------------------------------");
        System.out.println("这里是" + username + "的个人主页!");
        System.out.println("-------------------------------------");
        System.out.println("a.好友列表");
        System.out.println("b.添加好友");
        System.out.println("c.删除好友");
        System.out.println("d.私聊");
        System.out.println("e.处理好友申请");
        System.out.println("d.退出");
        System.out.println("--------------------------------------");
        Scanner sc = new Scanner(System.in);
        char c = sc.next().charAt(0);
        switch (c) {
            case 'a':
                break;
            case 'b':
                addFriend(username);
                break;
            case 'e':
                friendRequest(username);
                break;
        }
    }

    public void addFriend(String username) throws InterruptedException {
        System.out.println("请输入你要添加的好友用户名:");
        String friendName = sc.nextLine();
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode node = objectMapper.createObjectNode();
        node.put("username", username);
        node.put("friendName", friendName);
        node.put("type", String.valueOf(MsgType.MSG_FRIEND_REQUEST));
        String msg = node.toString();

        channel.writeAndFlush(msg);

        int code = ClientHandler.queue.take();
        if(code == 100) {
            System.out.println("已发送好友申请!");
        }
        else if (code == 200) {
            System.out.println("不存在该用户!");
        }
    }

    public void friendRequest(String username) throws InterruptedException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode node = objectMapper.createObjectNode();
        node.put("username", username);
        node.put("type", String.valueOf(MsgType.MSG_LIST_FRIEND_REQUEST));
        String msg = node.toString();

        channel.writeAndFlush(msg);

        String fromUsers = (String) ClientHandler.queue2.take();
        List<String> fromUsers2 = objectMapper.readValue(fromUsers, new TypeReference<List<String>>() {});
        for (String fromUser : fromUsers2) {
            System.out.println(fromUser + "发送了好友申请!");
        }
    }

    public void acceptFriendRequest(String username) throws InterruptedException{

    }

}