package com.noregret.Client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.noregret.MsgType;
import com.noregret.Server.pojo.Member;
import io.netty.channel.Channel;
import org.apache.commons.mail.HtmlEmail;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

public class SendService {
    private Channel channel;
    Scanner sc = new Scanner(System.in);

    public SendService(Channel channel) {
        this.channel = channel;
    }

    public void menu() throws InterruptedException, IOException {
        System.out.println("-----------------------------------------");
        System.out.println("              欢迎进入聊天室                ");
        System.out.println("-----------------------------------------");
        System.out.println("           a.登录       b.注册             ");
        System.out.println("          c.找回密码     d.注销             ");
        System.out.println("                 e.退出                    ");
        while (true) {
            System.out.println("------------------------------------------");
            System.out.println("请输入选择:");
            char c = sc.nextLine().charAt(0);
            switch (c) {
                case 'a':
                    login();
                    break;
                case 'b':
                    register();
                    break;
                case 'c':
                    find();
                case 'e':
                    System.exit(1);
            }
        }
    }

    public void login() throws InterruptedException, IOException {
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
        System.out.println("请输入用户名:(不超过10位)");
        String username = sc.nextLine();
        System.out.println("请输入密码:(不超过16位)");
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

    public void find() throws InterruptedException {
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
        String password = sc.nextLine();
        node.put("password", password);
        String msg = node.toString();
        channel.writeAndFlush(msg);
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
        System.out.println("-------------------------------------");
        System.out.println("这里是" + username + "的个人主页!");
        System.out.println("-------------------------------------");
        System.out.println("a.添加好友");
        System.out.println("b.好友列表");
        System.out.println("c.处理好友申请");
        System.out.println("d.创建群组");
        System.out.println("e.加入群组");
        System.out.println("f.群组列表");
        System.out.println("q.退出登录");
        while (true) {
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
        String msg = node.toString();
        channel.writeAndFlush(msg);

        int code = ClientHandler.queue.take();
        if (code == 100) {
            System.out.println("已发送好友申请!");
        } else if (code == 200) {
            System.out.println("不存在该用户!");
        } else if (code == 300) {
            System.out.println("已添加该好友!");
        }
    }

    public void listFriend(String username) throws InterruptedException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode node = objectMapper.createObjectNode();
        node.put("username", username);
        node.put("type", String.valueOf(MsgType.MSG_LIST_FRIEND));
        String msg = node.toString();
        channel.writeAndFlush(msg);

        String friends2 = (String) ClientHandler.queue2.take();
        List<String> friends = objectMapper.readValue(friends2, new TypeReference<>() {
        });
        if (!friends.isEmpty()) {
            int i = 1;
            HashMap<Integer, String> map = new HashMap<>();
            System.out.println("--------------------------");
            System.out.println(username + "的好友");
            System.out.println("--------------------------");
            for (String friend : friends) {
                System.out.println(i + "." + friend);
                map.put(i, friend);
                i++;
            }
            System.out.println("--------------------------");
            System.out.println("输入你要选择的好友序号:(按q返回个人主页)");
            char c = sc.nextLine().charAt(0);
            if (c == 'q') {
                personHome(username);
                return;
            }
            friendMenu(username, map.get(Character.getNumericValue(c)));
        } else {
            System.out.println("你当前未加好友!");
        }
    }

    public void friendMenu(String username, String friendName) throws InterruptedException, IOException {
        System.out.println("----------------------------");
        System.out.println("好友 " + friendName);
        System.out.println("----------------------------");
        System.out.println("a.私聊");
        System.out.println("d.删除该好友");
        System.out.println("q.返回好友列表");
        while (true) {
            System.out.println("----------------------------");
            System.out.println("请输入选择:");
            char c = sc.nextLine().charAt(0);
            switch (c) {
                case 'a':
                    privateChat(username, friendName);
                    break;
                case 'd':
                    deleteFriend(username, friendName);
                    personHome(username);
                    return;
                case 'q':
                    listFriend(username);
                    return;
            }
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
        List<String> fromUsers2 = objectMapper.readValue(fromUsers, new TypeReference<>() {
        });
        if (!fromUsers2.isEmpty()) {
            HashMap<Integer, String> map = new HashMap<>();
            int i = 1;
            for (String fromUser : fromUsers2) {
                map.put(i, fromUser);
                System.out.println(i + "." + fromUser + "发送了好友申请!");
                i++;
            }
            System.out.println("请输入你要处理的好友申请(序号):");
            Integer num = sc.nextInt();
            String fromUser = map.get(num);
            sc.nextLine();
            System.out.println("a.同意  b.拒绝");
            char choice = sc.nextLine().charAt(0);
            if (choice == 'a') {
                friendResponse(fromUser, username, 0);
            } else if (choice == 'b') {
                friendResponse(fromUser, username, 1);
            }
        } else {
            System.out.println("无新的好友申请!");
        }
    }

    public void friendResponse(String fromUser, String toUser, int code) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode node = objectMapper.createObjectNode();
        node.put("type", String.valueOf(MsgType.MSG_FRIEND_RESPONSE));
        node.put("fromUser", fromUser);
        node.put("toUser", toUser);
        node.put("code", code);
        String msg = node.toString();
        channel.writeAndFlush(msg);
    }

    public void deleteFriend(String username, String friendName) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode node = objectMapper.createObjectNode();
        node.put("username", username);
        node.put("friendName", friendName);
        node.put("type", String.valueOf(MsgType.MSG_DELETE_FRIEND));
        String msg = node.toString();
        channel.writeAndFlush(msg);
    }

    public void offline(String username) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode node = objectMapper.createObjectNode();
        node.put("username", username);
        node.put("type", String.valueOf(MsgType.MSG_OFFLINE));
        String msg = node.toString();
        channel.writeAndFlush(msg);
    }

    public void privateChat(String username, String friendName) throws InterruptedException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode node = objectMapper.createObjectNode();
        node.put("username", username);
        node.put("friendName", friendName);
        node.put("type", String.valueOf(MsgType.MSG_PRIVATE_CHAT));
        String msg = node.toString();
        channel.writeAndFlush(msg);

        int code = ClientHandler.queue.take();
        if (code == 100) {
            System.out.println("你与对方还不是好友!");
        } else {
            if (code == 200) {
                System.out.println("对方当前在线!可以开始聊天!");
            } else if (code == 300) {
                System.out.println("对方当前不在线!等待对方上线后接收消息!");
            }
            while (true) {
                String content = sc.nextLine();
                Timestamp time = new Timestamp(System.currentTimeMillis());
                System.out.println(time);
                node = objectMapper.createObjectNode();
                node.put("fromUser", username);
                node.put("toUser", friendName);
                node.put("content", content);
                node.put("time", time.toString());
                node.put("code", code);
                node.put("type", String.valueOf(MsgType.MSG_SAVE_MESSAGE));
                msg = node.toString();
                channel.writeAndFlush(msg);

                if ("bye".equals(content)) {
                    break;
                }
            }
        }
    }

    public void createGroup(String username) throws InterruptedException {
        System.out.println("请输入群组名称(不超过10位):");
        String groupName = sc.nextLine();
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode node = objectMapper.createObjectNode();
        node.put("groupName", groupName);
        node.put("member", username);
        node.put("type", String.valueOf(MsgType.MSG_CREATE_GROUP));
        String msg = node.toString();
        channel.writeAndFlush(msg);

        int code = ClientHandler.queue.take();
        if (code == 100) {
            System.out.println("该名称已被占用!请换一个吧!");
        } else if (code == 200) {
            System.out.println("建群成功!");
        }
    }

    public void addGroup(String username) throws InterruptedException {
        System.out.println("请输入你想要加入群聊的名称:");
        String groupName = sc.nextLine();
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode node = objectMapper.createObjectNode();
        node.put("groupName", groupName);
        node.put("member", username);
        node.put("type", String.valueOf(MsgType.MSG_GROUP_REQUEST));
        String msg = node.toString();
        channel.writeAndFlush(msg);

        int code = ClientHandler.queue.take();
        if (code == 100) {
            System.out.println("该群组不存在!");
        } else if (code == 200) {
            System.out.println("已发送加群申请!");
        }
    }

    public void listGroup(String username) throws InterruptedException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode node = objectMapper.createObjectNode();
        node.put("username", username);
        node.put("type", String.valueOf(MsgType.MSG_LIST_GROUP));
        String msg = node.toString();
        channel.writeAndFlush(msg);

        String groups2 = (String) ClientHandler.queue2.take();
        List<String> groups = objectMapper.readValue(groups2, new TypeReference<>() {
        });
        if (!groups.isEmpty()) {
            int i = 1;
            HashMap<Integer, String> map = new HashMap<>();
            System.out.println("-------------------------");
            System.out.println(username + "的群组");
            System.out.println("-------------------------");
            for (String group : groups) {
                System.out.println(i + "." + group);
                map.put(i, group);
                i++;
            }
            System.out.println("-------------------------");
            System.out.println("请输入你要选择的群组序号:(按q返回个人主页)");
            char c = sc.nextLine().charAt(0);
            if (c == 'q') {
                personHome(username);
                return;
            }
            groupMenu(username, map.get(Character.getNumericValue(c)));
        } else {
            System.out.println("你尚未加入群组!");
        }
    }

    public void groupMenu(String username, String groupName) throws InterruptedException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode node = objectMapper.createObjectNode();
        node.put("groupName", groupName);
        node.put("type", String.valueOf(MsgType.MSG_GROUP_MEMBER));
        String msg = node.toString();
        channel.writeAndFlush(msg);

        String members2 = (String) ClientHandler.queue2.take();
        List<Member> members = objectMapper.readValue(members2, new TypeReference<>() {
        });
        int role = 0;
        for (Member member : members) {
            if (member.getMember().equals(username)) {
                role = member.getRole();
            }
        }
        System.out.println("-----------------------------");
        System.out.println("群组 " + groupName);
        System.out.println("-----------------------------");
        System.out.println("a.群成员");
        System.out.println("b.群聊");
        if (role == 2 || role == 3) {
            System.out.println("c.退出群组");
        } else {
            System.out.println("d.处理加群申请");
            if (role == 1) {
                System.out.println("e.解散群组");
            }
        }
        System.out.println("q.返回群组列表");
        while (true) {
            System.out.println("------------------------------");
            System.out.println("请输入选择:");
            char c = sc.nextLine().charAt(0);
            switch (c) {
                case 'a':
                    listMember(username, groupName, role, members);
                    break;
                case 'c':
                    quitGroup(username, groupName);
                    listGroup(username);
                    return;
                case 'd':
                    groupRequest(groupName);
                    break;
                case 'e':
                    breakGroup(groupName);
                    listGroup(username);
                    return;
                case 'q':
                    listGroup(username);
                    return;
            }
        }
    }

    public void quitGroup(String username, String groupName) throws InterruptedException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode node = objectMapper.createObjectNode();
        node.put("groupName", groupName);
        node.put("username", username);
        node.put("type", String.valueOf(MsgType.MSG_QUIT_GROUP));
        String msg = node.toString();
        channel.writeAndFlush(msg);
        System.out.println("已退出该群组!");
    }

    public void groupRequest(String groupName) throws InterruptedException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode node = objectMapper.createObjectNode();
        node.put("groupName", groupName);
        node.put("type", String.valueOf(MsgType.MSG_LIST_GROUP_REQUEST));
        String msg = node.toString();
        channel.writeAndFlush(msg);

        String fromUsers2 = (String) ClientHandler.queue2.take();
        List<String> fromUsers = objectMapper.readValue(fromUsers2, new TypeReference<>() {
        });
        if (!fromUsers.isEmpty()) {
            int i = 1;
            HashMap<Integer, String> map = new HashMap<>();
            for (String fromUser : fromUsers) {
                map.put(i, fromUser);
                System.out.println(i + "." + fromUser + "申请加入该群聊!");
                i++;
            }
            System.out.println("请输入你要处理的加群申请(序号):");
            Integer num = sc.nextInt();
            String fromUser = map.get(num);
            sc.nextLine();
            System.out.println("a.同意  b.拒绝");
            char choice = sc.nextLine().charAt(0);
            if (choice == 'a') {
                groupResponse(fromUser, groupName, 0);
            } else if (choice == 'b') {
                groupResponse(fromUser, groupName, 1);
            }
        } else {
            System.out.println("无新的加群申请!");
        }
    }

    public void groupResponse(String fromUser, String groupName, int code) throws InterruptedException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode node = objectMapper.createObjectNode();
        node.put("fromUser", fromUser);
        node.put("groupName", groupName);
        node.put("code",code);
        node.put("type", String.valueOf(MsgType.MSG_GROUP_RESPONSE));
        String msg = node.toString();
        channel.writeAndFlush(msg);
    }


    public void breakGroup(String groupName) throws InterruptedException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode node = objectMapper.createObjectNode();
        node.put("groupName", groupName);
        node.put("type", String.valueOf(MsgType.MSG_BREAK_GROUP));
        String msg = node.toString();
        channel.writeAndFlush(msg);
    }

    public void listMember(String username, String groupName, int role, List<Member> members) throws IOException, InterruptedException {
        System.out.println("群组 " + groupName + " 成员");
        int i = 1, j = 1, k = 1;
        HashMap<Integer, String> map = new HashMap<>();
        HashMap<String, Integer> map2 = new HashMap<>();
        for (Member member : members) {
            map.put(k, member.getMember());
            map2.put(member.getMember(), member.getRole());
            if (member.getRole() == 1) {
                System.out.println("群主:");
                System.out.println(k + "." + member.getMember());
            } else if (member.getRole() == 2) {
                if (i == 1) {
                    System.out.println("管理员:");
                    System.out.println(k + "." + member.getMember());
                } else {
                    System.out.println(k + "." + member.getMember());
                }
                i++;
            } else {
                if (j == 1) {
                    System.out.println("普通成员:");
                    System.out.println(k + "." + member.getMember());
                } else {
                    System.out.println(k + "." + member.getMember());
                }
                j++;
            }
            k++;
        }
        if (role == 1 || role == 2) {
            System.out.println("a.移除成员");
        }
        if (role == 1) {
            System.out.println("b.设置管理员");
            System.out.println("c.取消管理员");
        }
        System.out.println("q.返回上层");
        while (true) {
            System.out.println("请输入选择:");
            char c = sc.nextLine().charAt(0);
            int num = 0;
            switch (c) {
                case 'a':
                    System.out.println("请输入你要移除的成员序号:");
                    num = sc.nextInt();
                    sc.nextLine();
                    if (role == 2 && (map2.get(map.get(num)) == 1 || map2.get(map.get(num)) == 2)) {
                        System.out.println("管理员只能移除普通成员");
                    }
                    removeMember(groupName, map.get(num));
                    break;
                case 'b':
                    System.out.println("请输入你要添加管理员的成员序号:");
                    num = sc.nextInt();
                    sc.nextLine();
                    addManager(groupName, map.get(num));
                    break;
                case 'c':
                    System.out.println("请输入你要取消管理员的成员序号:");
                    num = sc.nextInt();
                    sc.nextLine();
                    removeManager(groupName, map.get(num));
                    break;
                case 'q':
                    groupMenu(username, groupName);
                    return;
            }
        }
    }

    public void removeMember(String groupName, String memberName) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode node = objectMapper.createObjectNode();
        node.put("groupName", groupName);
        node.put("memberName", memberName);
        node.put("type", String.valueOf(MsgType.MSG_REMOVE_MEMBER));
        String msg = node.toString();
        channel.writeAndFlush(msg);
    }

    public void addManager(String groupName, String memberName) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode node = objectMapper.createObjectNode();
        node.put("groupName", groupName);
        node.put("memberName", memberName);
        node.put("type", String.valueOf(MsgType.MSG_ADD_MANAGER));
        String msg = node.toString();
        channel.writeAndFlush(msg);
    }

    public void removeManager(String groupName, String memberName) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode node = objectMapper.createObjectNode();
        node.put("groupName", groupName);
        node.put("memberName", memberName);
        node.put("type", String.valueOf(MsgType.MSG_REMOVE_MANAGER));
        String msg = node.toString();
        channel.writeAndFlush(msg);
    }
}