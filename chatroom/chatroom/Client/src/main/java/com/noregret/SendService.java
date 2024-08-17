package com.noregret;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import org.apache.commons.mail.HtmlEmail;
import com.noregret.Pojo.*;

import java.io.*;
import java.sql.Timestamp;
import java.util.*;

public class SendService {
    private final Channel channel;
    Scanner sc = new Scanner(System.in);
    Console console = System.console();

    public SendService(Channel channel) {
        this.channel = channel;
    }

    public void menu() throws InterruptedException, IOException {
        while (true) {
            System.out.println("-----------------------------------------");
            System.out.println("              欢迎进入聊天室                ");
            System.out.println("-----------------------------------------");
            System.out.println("                 a.登录                   ");
            System.out.println("                 b.注册                    ");
            System.out.println("                c.忘记密码                  ");
            System.out.println("                 q.退出                    ");
            System.out.println("------------------------------------------");
            System.out.println("请输入选择:");
            char c = sc.next().charAt(0);
            sc.nextLine();
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
                case 'q':
                    System.exit(1);
            }
        }
    }

    public void send(ObjectNode node) {
        byte[] msg = node.toString().getBytes();
        int length = msg.length;
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        buf.writeInt(length);
        buf.writeBytes(msg);
        channel.writeAndFlush(buf);
    }

    public void login() throws InterruptedException, IOException {
        System.out.println("请输入用户名:");
        String username = sc.nextLine();
        System.out.println("请输入密码:");
        char[] passwordArray = console.readPassword();
        String password = new String(passwordArray);

        //封装json数据
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode node = objectMapper.createObjectNode();
        node.put("username", username);
        node.put("password", password);
        node.put("type", String.valueOf(MsgType.MSG_LOGIN));

        //发送至服务器
        send(node);

        int code = ClientHandler.queue.take();
        if (code == 100) {
            System.out.println("登录成功!");
            personHome(username);
        } else if (code == 200) {
            System.out.println("密码错误!");
        } else if (code == 300) {
            System.out.println("该用户不存在!");
        } else if (code == 400) {
            System.out.println("您已在另一台设备上登录!");
        }
    }

    public void register() throws InterruptedException {
        System.out.println("请输入用户名:(不超过20位)");
        String username = sc.nextLine();
        System.out.println("请输入密码:(不超过20位)");
        char[] passwordArray = console.readPassword();
        String password = new String(passwordArray);

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode node = objectMapper.createObjectNode();
        node.put("username", username);
        node.put("password", password);
        node.put("type", String.valueOf(MsgType.MSG_REGISTER));

        send(node);

        int code = ClientHandler.queue.take();
        if (code == 100) {
            System.out.println("注册成功!");
        } else if (code == 200) {
            System.out.println("用户名已存在!");
        }
    }

    public void find() {
        System.out.println("请输入用户名:");
        String username = sc.nextLine();
        System.out.println("请输入qq邮箱:");
        String email = sc.nextLine();
        String checkCode = random1();
        sendEmail(email, checkCode);
        System.out.println("请输入验证码:");
        String checkCode2 = sc.nextLine();
        while (!checkCode.equals(checkCode2)) {
            System.out.println("验证码错误!");
            checkCode2 = sc.nextLine();
        }
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode node = objectMapper.createObjectNode();
        node.put("type", String.valueOf(MsgType.MSG_FIND));
        node.put("username", username);
        System.out.println("请输入新密码:");
        char[] passwordArray = console.readPassword();
        String password = new String(passwordArray);
        node.put("password", password);

        send(node);
    }

    public void sendEmail(String email, String checkCode) {
        HtmlEmail send = new HtmlEmail();
        try {
            send.setHostName("smtp.qq.com");
            send.setAuthentication("2323656816@qq.com", "omugfnybgaoteccf");
            send.setFrom("2323656816@qq.com", "noregret.chatroom");
            send.setSmtpPort(465);
            send.setSSLOnConnect(true);
            send.setCharset("UTF-8");
            send.addTo(email);
            send.setSubject("验证码");
            send.setMsg("你好!你的验证码为" + checkCode);
            send.send();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String random1() {
        StringBuilder code = new StringBuilder();
        Random rd = new Random();
        for (int i = 0; i < 6; i++) {
            int r = rd.nextInt(10); //每次随机出一个数字（0-9）
            code.append(r);  //把每次随机出的数字拼在一起
        }
        return code.toString();
    }

    public void personHome(String username) throws InterruptedException, IOException {
        while (true) {
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode node = objectMapper.createObjectNode();
            node.put("username", username);
            node.put("type", String.valueOf(MsgType.MSG_HOME));
            send(node);

            String fromUsers2 = (String) ClientHandler.queue2.take();
            List<String> fromUsers = objectMapper.readValue(fromUsers2, new TypeReference<>() {
            });
            String requests2 = (String) ClientHandler.queue2.take();
            List<Request> requests = objectMapper.readValue(requests2, new TypeReference<>() {
            });
            String friends2 = (String) ClientHandler.queue2.take();
            List<String> friends = objectMapper.readValue(friends2, new TypeReference<>() {
            });
            System.out.println("-------------------------------------");
            System.out.println("这里是 " + username + " 的个人主页!");
            System.out.println("-------------------------------------");
            System.out.println("a.添加好友");
            System.out.println("b.好友列表");
            System.out.println("c.处理好友申请");
            System.out.println("d.创建群组");
            System.out.println("e.加入群组");
            System.out.println("f.群组列表");
            System.out.println("g.处理加群申请");
            System.out.println("h.接收文件");
            System.out.println("i.注销帐号");
            System.out.println("q.退出登录");
            System.out.println("z.刷新");
            if (!fromUsers.isEmpty()) {
                for (String fromUser : fromUsers) {
                    System.out.println(Utils.getColoredString(33, 1, fromUser + " 申请添加为好友!"));
                }
            }
            if (!requests.isEmpty()) {
                for (Request request : requests) {
                    System.out.println(Utils.getColoredString(33, 1, request.getFrom() + " 申请加入 " + request.getTo() + " 群组!"));
                }
            }
            HashMap<String, Integer> map = new HashMap<>();
            if (!friends.isEmpty()) {
                for (String friend : friends) {
                    if (!map.containsKey(friend)) {
                        System.out.println(Utils.getColoredString(33, 1, friend + " 发送了新消息!"));
                    }
                    map.put(friend, map.getOrDefault(friend, 0) + 1);
                }
            }
            System.out.println("--------------------------------------");
            System.out.println("请输入选择:");
            Scanner sc = new Scanner(System.in);
            char c = sc.next().charAt(0);
            switch (c) {
                case 'a':
                    addFriend(username);
                    break;
                case 'b':
                    listFriend(username);
                    break;
                case 'c':
                    friendRequest(username);
                    break;
                case 'd':
                    createGroup(username);
                    break;
                case 'e':
                    addGroup(username);
                    break;
                case 'f':
                    listGroup(username);
                    break;
                case 'g':
                    groupRequest(username);
                    break;
                case 'h':
                    receiveFile(username);
                    break;
                case 'i':
                    deleteUser(username);
                    menu();
                    return;
                case 'z':
                    break;
                case 'q':
                    offline(username);
                    menu();
                    return;
            }
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
        send(node);

        int code = ClientHandler.queue.take();
        if (code == 100) {
            System.out.println("已发送好友申请!");
        } else if (code == 200) {
            System.out.println("不存在该用户!");
        } else if (code == 300) {
            System.out.println("已添加该好友!");
        } else if (code == 400) {
            System.out.println("已发送过好友申请!");
        }
    }

    public void listFriend(String username) throws InterruptedException, IOException {
        while (true) {
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode node = objectMapper.createObjectNode();
            node.put("username", username);
            node.put("type", String.valueOf(MsgType.MSG_LIST_FRIEND));
            send(node);

            String friends2 = (String) ClientHandler.queue2.take();
            List<theFriend> friends = objectMapper.readValue(friends2, new TypeReference<>() {
            });
            if (!friends.isEmpty()) {
                int i = 1;
                HashMap<Integer, String> map = new HashMap<>();
                System.out.println("--------------------------");
                System.out.println(username + " 的好友");
                System.out.println("--------------------------");
                for (theFriend friend : friends) {
                    System.out.print(i + "." + friend.getName());
                    if (friend.getStatus() == 1) {
                        System.out.println(" 在线");
                    } else {
                        System.out.println(" 离线");
                    }
                    map.put(i, friend.getName());
                    i++;
                }
                System.out.println("--------------------------");
                System.out.println("输入你要选择的好友序号:(q:返回个人主页 z:刷新)");
                char c = sc.next().charAt(0);
                sc.nextLine();
                if (c == 'q') {
                    personHome(username);
                    return;
                } else if (c == 'z') {
                    continue;
                }
                friendMenu(username, map.get(Character.getNumericValue(c)));
            } else {
                System.out.println("你当前未加好友!(q:返回个人主页 z:刷新)");
                char c = sc.next().charAt(0);
                sc.nextLine();
                if (c == 'q') {
                    personHome(username);
                    return;
                }
            }
        }
    }

    public void friendMenu(String username, String friendName) throws InterruptedException, IOException {
        while (true) {
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode node = objectMapper.createObjectNode();
            node.put("username", username);
            node.put("friendName", friendName);
            node.put("type", String.valueOf(MsgType.MSG_FRIEND_MENU));
            send(node);

            int status = ClientHandler.queue.take();
            System.out.println("----------------------------");
            System.out.println("好友 " + friendName);
            if (status == 1) {
                System.out.println("(已屏蔽)");
            }
            System.out.println("----------------------------");
            System.out.println("a.私聊");
            System.out.println("b.发送文件");
            if (status == 0) {
                System.out.println("c.屏蔽该好友");
            } else if (status == 1) {
                System.out.println("c.取消屏蔽");
            }
            System.out.println("d.查看历史消息");
            System.out.println("f.删除该好友");
            System.out.println("q.返回好友列表");
            System.out.println("z.刷新");
            System.out.println("----------------------------");
            System.out.println("请输入选择:");
            char c = sc.next().charAt(0);
            sc.nextLine();
            switch (c) {
                case 'a':
                    privateChat(username, friendName);
                    break;
                case 'b':
                    sendFile(username, friendName);
                    break;
                case 'c':
                    if (status == 0) {
                        block(username, friendName);
                    } else if (status == 1) {
                        unblock(username, friendName);
                    }
                    break;
                case 'd':
                    friendMessage(username, friendName);
                    break;
                case 'f':
                    deleteFriend(username, friendName);
                    listFriend(username);
                    return;
                case 'q':
                    listFriend(username);
                    return;
                case 'z':
                    break;
            }
        }
    }

    public void friendRequest(String username) throws InterruptedException, IOException {
        while (true) {
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode node = objectMapper.createObjectNode();
            node.put("username", username);
            node.put("type", String.valueOf(MsgType.MSG_LIST_FRIEND_REQUEST));
            send(node);

            String fromUsers = (String) ClientHandler.queue2.take();
            List<String> fromUsers2 = objectMapper.readValue(fromUsers, new TypeReference<>() {
            });
            if (!fromUsers2.isEmpty()) {
                HashMap<Integer, String> map = new HashMap<>();
                int i = 1;
                for (String fromUser : fromUsers2) {
                    map.put(i, fromUser);
                    System.out.println(i + "." + fromUser + " 发送了好友申请!");
                    i++;
                }
                System.out.println("请输入你要处理的好友申请序号:(q:返回个人主页 z:刷新)");
                char c = sc.next().charAt(0);
                sc.nextLine();
                if (c == 'q') {
                    personHome(username);
                    return;
                } else if (c == 'z') {
                    continue;
                }
                String fromUser = map.get(Character.getNumericValue(c));
                System.out.println("a.同意  b.拒绝");
                char choice = sc.next().charAt(0);
                sc.nextLine();
                if (choice == 'a') {
                    friendResponse(fromUser, username, 0);
                } else if (choice == 'b') {
                    friendResponse(fromUser, username, 1);
                }
            } else {
                System.out.println("无新的好友申请!(q:返回个人主页 z:刷新)");
                char c = sc.next().charAt(0);
                sc.nextLine();
                if (c == 'q') {
                    personHome(username);
                    return;
                }
            }
        }
    }

    public void friendResponse(String fromUser, String toUser, int code) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode node = objectMapper.createObjectNode();
        node.put("type", String.valueOf(MsgType.MSG_FRIEND_RESPONSE));
        node.put("fromUser", fromUser);
        node.put("toUser", toUser);
        node.put("code", code);
        send(node);
    }

    public void deleteFriend(String username, String friendName) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode node = objectMapper.createObjectNode();
        node.put("username", username);
        node.put("friendName", friendName);
        node.put("type", String.valueOf(MsgType.MSG_DELETE_FRIEND));
        send(node);
    }

    public void offline(String username) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode node = objectMapper.createObjectNode();
        node.put("username", username);
        node.put("type", String.valueOf(MsgType.MSG_OFFLINE));
        send(node);
    }

    public void deleteUser(String username) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode node = objectMapper.createObjectNode();
        node.put("username", username);
        node.put("type", String.valueOf(MsgType.MSG_DELETE_USER));
        send(node);
    }

    public void privateChat(String username, String friendName) throws InterruptedException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        System.out.println("开始聊天!(按q退出)");
        ObjectNode node;
        while (true) {
            String content = sc.nextLine();
            Timestamp time = new Timestamp(System.currentTimeMillis());
            node = objectMapper.createObjectNode();
            node.put("fromUser", username);
            node.put("toUser", friendName);
            node.put("content", content);
            node.put("time", time.toString());
            node.put("type", String.valueOf(MsgType.MSG_PRIVATE_CHAT));
            send(node);
            if ("q".equals(content)) {
                break;
            }

            int status = ClientHandler.queue.take();
            if (status == 1) {
                System.out.println(Utils.getColoredString(31, 1, "你已被对方屏蔽!"));
            } else if (status == 0) {
                System.out.println(time.toString().substring(0, 19) + " " + username + ":" + content);
            }
        }

    }

    public void sendFile(String username, String friendName) throws IOException, InterruptedException {
        while (true) {
            System.out.println("请输入你要传输的文件:(按q返回上层)");
            String fileURL = sc.nextLine();
            if ("q".equals(fileURL)) {
                friendMenu(username, friendName);
                return;
            }
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode node = objectMapper.createObjectNode();
            node.put("from", username);
            node.put("to", friendName);
            String[] urlComponents = fileURL.split("/");
            if (urlComponents.length > 0) {
                node.put("filename", urlComponents[urlComponents.length - 1]);
            }
            node.put("type", String.valueOf(MsgType.MSG_SEND_FILE));
            Timestamp time = new Timestamp(System.currentTimeMillis());
            node.put("time", time.toString());
            try {
                File file = new File(fileURL);
                int port = ClientHandler.queue.take();
                String ip = Utils.getIP();
                new SendFileThread(port, ip, file).start();
                send(node);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void friendMessage(String username, String friendName) throws InterruptedException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode node = objectMapper.createObjectNode();
        node.put("username", username);
        node.put("friendName", friendName);
        node.put("type", String.valueOf(MsgType.MSG_FRIEND_MESSAGE));
        send(node);

        String messages2 = (String) ClientHandler.queue2.take();
        List<Message> messages = objectMapper.readValue(messages2, new TypeReference<>() {
        });
        if (!messages.isEmpty()) {
            boolean print = false;
            for (Message message : messages) {
                if (message.getStatus().toString().equals("unread") && !print) {
                    System.out.println(Utils.getColoredString(33, 1, "新消息:"));
                    print = true;
                }
                System.out.println(message.getTime().toString().substring(0, 19) + " " + message.getFrom() + ":" + message.getContent());
            }
        }
    }

    public void block(String username, String friendName) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode node = objectMapper.createObjectNode();
        node.put("username", username);
        node.put("friendName", friendName);
        node.put("type", String.valueOf(MsgType.MSG_BLOCK));
        send(node);
    }

    public void unblock(String username, String friendName) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode node = objectMapper.createObjectNode();
        node.put("username", username);
        node.put("friendName", friendName);
        node.put("type", String.valueOf(MsgType.MSG_UNBLOCK));
        send(node);
    }

    public void createGroup(String username) throws InterruptedException {
        System.out.println("请输入群组名称(不超过20位):");
        String groupName = sc.nextLine();
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode node = objectMapper.createObjectNode();
        node.put("groupName", groupName);
        node.put("member", username);
        node.put("type", String.valueOf(MsgType.MSG_CREATE_GROUP));
        send(node);

        int code = ClientHandler.queue.take();
        if (code == 100) {
            System.out.println("该名称已被占用!请换一个吧!");
        } else if (code == 200) {
            System.out.println("建群成功!");
        }
    }

    public void addGroup(String username) throws InterruptedException {
        System.out.println("请输入你想要加入群组的名称:");
        String groupName = sc.nextLine();
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode node = objectMapper.createObjectNode();
        node.put("groupName", groupName);
        node.put("member", username);
        node.put("type", String.valueOf(MsgType.MSG_GROUP_REQUEST));
        send(node);

        int code = ClientHandler.queue.take();
        if (code == 100) {
            System.out.println("该群组不存在!");
        } else if (code == 200) {
            System.out.println("已发送加群申请!");
        } else if (code == 300) {
            System.out.println("已发送过加群申请!");
        } else if (code == 400) {
            System.out.println("已加入该群组!");
        }
    }

    public void listGroup(String username) throws InterruptedException, IOException {
        while (true) {
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode node = objectMapper.createObjectNode();
            node.put("username", username);
            node.put("type", String.valueOf(MsgType.MSG_LIST_GROUP));
            send(node);

            String groups2 = (String) ClientHandler.queue2.take();
            List<String> groups = objectMapper.readValue(groups2, new TypeReference<>() {
            });
            if (!groups.isEmpty()) {
                int i = 1;
                HashMap<Integer, String> map = new HashMap<>();
                System.out.println("-------------------------");
                System.out.println(username + " 的群组");
                System.out.println("-------------------------");
                for (String group : groups) {
                    System.out.println(i + "." + group);
                    map.put(i, group);
                    i++;
                }
                System.out.println("-------------------------");
                System.out.println("请输入你要选择的群组序号:(q:返回个人主页 z:刷新)");
                char c = sc.next().charAt(0);
                sc.nextLine();
                if (c == 'q') {
                    personHome(username);
                    return;
                } else if (c == 'z') {
                    continue;
                }
                groupMenu(username, map.get(Character.getNumericValue(c)));
            } else {
                System.out.println("你尚未加入群组!(q:返回个人主页 z:刷新)");
                char c = sc.next().charAt(0);
                sc.nextLine();
                if (c == 'q') {
                    personHome(username);
                    return;
                }
            }
        }
    }

    public void groupMenu(String username, String groupName) throws InterruptedException, IOException {
        while (true) {
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode node = objectMapper.createObjectNode();
            node.put("groupName", groupName);
            node.put("username", username);
            node.put("type", String.valueOf(MsgType.MSG_MEMBER_ROLE));
            send(node);

            int role = ClientHandler.queue.take();
            System.out.println("-----------------------------");
            System.out.println("群组 " + groupName);
            System.out.println("-----------------------------");
            System.out.println("a.群成员");
            System.out.println("b.群聊");
            // System.out.println("c.发送文件");
            if (role == 2 || role == 3) {
                System.out.println("d.退出群组");
            } else {
                if (role == 1) {
                    System.out.println("d.解散群组");
                }
            }
            System.out.println("e.查看历史消息");
            System.out.println("q.返回群组列表");
            System.out.println("z.刷新");
            System.out.println("------------------------------");
            System.out.println("请输入选择:");
            char c = sc.next().charAt(0);
            sc.nextLine();
            switch (c) {
                case 'a':
                    listMember(username, groupName, role);
                    break;
                case 'b':
                    groupChat(username, groupName);
                    break;
//                case 'c':
//                    sendGroupFile(username, groupName);
//                    break;
                case 'd':
                    if (role == 1) {
                        breakGroup(groupName);
                    } else {
                        quitGroup(username, groupName);
                    }
                    listGroup(username);
                    return;
                case 'e':
                    groupMessage(groupName);
                    break;
                case 'q':
                    listGroup(username);
                    return;
                case 'z':
                    break;
            }
        }
    }

    public void groupChat(String username, String groupName) throws InterruptedException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode node;
        System.out.println("开始聊天!(按q退出)");
        while (true) {
            String content = sc.nextLine();
            Timestamp time = new Timestamp(System.currentTimeMillis());
            node = objectMapper.createObjectNode();
            node.put("from", username);
            node.put("to", groupName);
            node.put("content", content);
            node.put("time", time.toString());
            node.put("type", String.valueOf(MsgType.MSG_GROUP_CHAT));
            send(node);
            if ("q".equals(content)) {
                break;
            }

            int status = ClientHandler.queue.take();
            if (status == 1) {
                System.out.println(Utils.getColoredString(31, 1, "你已被禁言!"));
            } else if (status == 0) {
                System.out.println(time.toString().substring(0, 19) + " " + username + ":" + content);
            }
        }
    }

    public void groupMessage(String groupName) throws InterruptedException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode node = objectMapper.createObjectNode();
        node.put("groupName", groupName);
        node.put("type", String.valueOf(MsgType.MSG_GROUP_MESSAGE));
        send(node);

        String messages = (String) ClientHandler.queue2.take();
        List<Message> messages2 = objectMapper.readValue(messages, new TypeReference<>() {
        });
        if (!messages2.isEmpty()) {
            for (Message message : messages2) {
                System.out.println(message.getTime().toString().substring(0, 19) + " " + message.getFrom() + ":" + message.getContent());
            }
        }
    }

    public void sendGroupFile(String username, String groupName) throws IOException, InterruptedException {
        System.out.println("请输入你要传输的文件:(按q返回上层)");
        String fileURL = sc.nextLine();
        if ("q".equals(fileURL)) {
            groupMenu(username, groupName);
            return;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode node = objectMapper.createObjectNode();
        node.put("from", username);
        node.put("to", groupName);
        String[] urlComponents = fileURL.split("/");
        if (urlComponents.length > 0) {
            node.put("filename", urlComponents[urlComponents.length - 1]);
        }
        node.put("type", String.valueOf(MsgType.MSG_SEND_GROUP_FILE));
        send(node);

        int status = ClientHandler.queue.take();
        if (status == 0) {
            try {
                File file = new File(fileURL);
                int port = ClientHandler.queue.take();
                String ip = Utils.getIP();
                new SendFileThread(port, ip, file).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (status == 1) {
            System.out.println("群组当前无人在线!");
        }
    }

    public void quitGroup(String username, String groupName) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode node = objectMapper.createObjectNode();
        node.put("groupName", groupName);
        node.put("username", username);
        node.put("type", String.valueOf(MsgType.MSG_QUIT_GROUP));
        send(node);
        System.out.println("已退出该群组!");
    }

    public void groupRequest(String username) throws InterruptedException, IOException {
        while (true) {
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode node = objectMapper.createObjectNode();
            node.put("username", username);
            node.put("type", String.valueOf(MsgType.MSG_LIST_GROUP_REQUEST));
            send(node);

            String requests2 = (String) ClientHandler.queue2.take();
            List<Request> requests = objectMapper.readValue(requests2, new TypeReference<>() {
            });
            if (!requests.isEmpty()) {
                int i = 1;
                HashMap<Integer, Request> map = new HashMap<>();
                for (Request request : requests) {
                    System.out.println(i + "." + request.getFrom() + " 申请加入 " + request.getTo() + " 群组!");
                    map.put(i, request);
                    i++;
                }
                System.out.println("请输入你要处理的加群申请序号:(q:退出 z:刷新)");
                char c = sc.next().charAt(0);
                sc.nextLine();
                if (c == 'q') {
                    personHome(username);
                    return;
                } else if (c == 'z') {
                    continue;
                }
                Request request = map.get(Character.getNumericValue(c));
                System.out.println("a.同意  b.拒绝");
                char c2 = sc.next().charAt(0);
                sc.nextLine();
                if (c2 == 'a') {
                    groupResponse(request.getFrom(), request.getTo(), 0);
                } else if (c2 == 'b') {
                    groupResponse(request.getFrom(), request.getTo(), 1);
                }
            } else {
                System.out.println("无新的加群申请!(按q返回个人主页)");
                char c = sc.next().charAt(0);
                sc.nextLine();
                if (c == 'q') {
                    personHome(username);
                    return;
                }
            }
        }
    }

    public void groupResponse(String fromUser, String groupName, int code) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode node = objectMapper.createObjectNode();
        node.put("fromUser", fromUser);
        node.put("groupName", groupName);
        node.put("code", code);
        node.put("type", String.valueOf(MsgType.MSG_GROUP_RESPONSE));
        send(node);
    }


    public void breakGroup(String groupName) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode node = objectMapper.createObjectNode();
        node.put("groupName", groupName);
        node.put("type", String.valueOf(MsgType.MSG_BREAK_GROUP));
        send(node);
    }

    public void listMember(String username, String groupName, int role) throws IOException, InterruptedException {
        while (true) {
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode node = objectMapper.createObjectNode();
            node.put("groupName", groupName);
            node.put("type", String.valueOf(MsgType.MSG_GROUP_MEMBER));
            send(node);

            String members2 = (String) ClientHandler.queue2.take();
            List<Member> members = objectMapper.readValue(members2, new TypeReference<>() {
            });
            System.out.println("----------------------------");
            System.out.println("群组 " + groupName + " 成员");
            System.out.println("----------------------------");
            int i = 1, j = 1, k = 1;
            HashMap<Integer, Member> map = new HashMap<>();
            for (Member member : members) {
                map.put(k, member);
                if (member.getRole() == 1) {
                    System.out.println(Utils.getColoredString(34, 1, "群主:"));
                    System.out.println(k + "." + member.getMember());
                    k++;
                }
                if (member.getRole() == 2) {
                    if (i == 1) {
                        System.out.println(Utils.getColoredString(34, 1, "管理员:"));
                        System.out.print(k + "." + member.getMember());
                        i++;
                    } else {
                        System.out.print(k + "." + member.getMember());
                    }
                    if (member.getStatus() == 1) {
                        System.out.println("(禁言中)");
                    } else {
                        System.out.println();
                    }
                    k++;
                }
            }
            for (Member member : members) {
                if (member.getRole() == 3) {
                    if (j == 1) {
                        System.out.println(Utils.getColoredString(34, 1, "普通成员:"));
                        System.out.print(k + "." + member.getMember());
                        j++;
                    } else {
                        System.out.print(k + "." + member.getMember());
                    }
                    if (member.getStatus() == 1) {
                        System.out.println("(禁言中)");
                    } else {
                        System.out.println();
                    }
                }
            }
            System.out.println("----------------------------");
            if (role == 1 || role == 2) {
                System.out.println("a.移除成员");
                System.out.println("b.禁言/取消禁言");
            }
            if (role == 1) {
                System.out.println("c.设置管理员");
                System.out.println("d.取消管理员");
            }
            System.out.println("q.返回上层");
            System.out.println("z.刷新");
            System.out.println("----------------------------");
            System.out.println("请输入选择:");
            char c = sc.next().charAt(0);
            sc.nextLine();
            int num, num2;
            Member member;
            switch (c) {
                case 'a':
                    if (role == 3) {
                        break;
                    }
                    System.out.println("请输入你要移除的成员序号:");
                    num = sc.next().charAt(0);
                    sc.nextLine();
                    num2 = Character.getNumericValue(num);
                    member = map.get(num2);
                    if (role == 2 && (member.getRole() == 1 || member.getRole() == 2)) {
                        System.out.println("管理员只能移除普通成员");
                        break;
                    } else if (member.getMember().equals(username)) {
                        System.out.println("不能移除自己");
                        break;
                    }
                    removeMember(groupName, member.getMember());
                    System.out.println("移除成功!");
                    break;
                case 'b':
                    if (role == 3) {
                        break;
                    }
                    System.out.println("请输入你要禁言或者取消禁言的成员序号:");
                    num = sc.next().charAt(0);
                    sc.nextLine();
                    num2 = Character.getNumericValue(num);
                    member = map.get(num2);
                    if (member.getMember().equals(username)) {
                        System.out.println("不能(取消)禁言自己");
                        break;
                    } else if (role == 2 && (member.getRole() == 1 || member.getRole() == 2)) {
                        System.out.println("管理员只能(取消)禁言普通成员");
                        break;
                    }
                    if (member.getStatus() == 0) {
                        blockMember(groupName, member.getMember());
                        System.out.println("禁言成功!");
                    } else {
                        unblockMember(groupName, member.getMember());
                        System.out.println("取消禁言成功!");
                    }
                    break;
                case 'c':
                    if (role == 2 || role == 3) {
                        break;
                    }
                    System.out.println("请输入你要添加管理员的成员序号:");
                    num = sc.next().charAt(0);
                    sc.nextLine();
                    num2 = Character.getNumericValue(num);
                    member = map.get(num2);
                    if (member.getRole() == 3) {
                        addManager(groupName, member.getMember());
                        System.out.println("添加成功!");
                    }
                    break;
                case 'd':
                    if (role == 2 || role == 3) {
                        break;
                    }
                    System.out.println("请输入你要取消管理员的成员序号:");
                    num = sc.next().charAt(0);
                    sc.nextLine();
                    num2 = Character.getNumericValue(num);
                    member = map.get(num2);
                    if (member.getRole() == 2) {
                        removeManager(groupName, member.getMember());
                        System.out.println("取消成功!");
                    }
                    break;
                case 'q':
                    groupMenu(username, groupName);
                    return;
                case 'z':
                    break;
            }
        }
    }

    public void removeMember(String groupName, String member) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode node = objectMapper.createObjectNode();
        node.put("groupName", groupName);
        node.put("member", member);
        node.put("type", String.valueOf(MsgType.MSG_REMOVE_MEMBER));
        send(node);
    }

    public void addManager(String groupName, String member) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode node = objectMapper.createObjectNode();
        node.put("groupName", groupName);
        node.put("member", member);
        node.put("type", String.valueOf(MsgType.MSG_ADD_MANAGER));
        send(node);
    }

    public void blockMember(String groupName, String member) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode node = objectMapper.createObjectNode();
        node.put("groupName", groupName);
        node.put("member", member);
        node.put("type", String.valueOf(MsgType.MSG_BLOCK_MEMBER));
        send(node);
    }

    public void unblockMember(String groupName, String member) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode node = objectMapper.createObjectNode();
        node.put("groupName", groupName);
        node.put("member", member);
        node.put("type", String.valueOf(MsgType.MSG_UNBLOCK_MEMBER));
        send(node);
    }

    public void removeManager(String groupName, String member) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode node = objectMapper.createObjectNode();
        node.put("groupName", groupName);
        node.put("member", member);
        node.put("type", String.valueOf(MsgType.MSG_REMOVE_MANAGER));
        send(node);
    }

    public void receiveFile(String username) throws InterruptedException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode node = objectMapper.createObjectNode();
        node.put("username", username);
        node.put("type", String.valueOf(MsgType.MSG_RECEIVE_FILE));
        send(node);

        String fileRequests = (String) ClientHandler.queue2.take();
        List<FileRequest> fileRequests1 = objectMapper.readValue(fileRequests, new TypeReference<>() {
        });

        if (!fileRequests1.isEmpty()) {
            int i = 1;
            HashMap<Integer, FileRequest> map = new HashMap<>();
            for (FileRequest fileRequest : fileRequests1) {
                map.put(i, fileRequest);
                System.out.println(i + "." + fileRequest.getFrom() + "发送的" + fileRequest.getFilename());
                i++;
            }
            System.out.println("选择你要接收的文件:");
            int choice = sc.nextInt();
            FileRequest fileRequest = map.get(choice);

            node = objectMapper.createObjectNode();
            node.put("fileID", fileRequest.getFileID());
            node.put("type", String.valueOf(MsgType.MSG_UPLOAD_FILE));
            send(node);

            int port = ClientHandler.queue.take();
            new RecvFileThread(port).start();
        }
    }

}