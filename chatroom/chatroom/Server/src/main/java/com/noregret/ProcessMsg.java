package com.noregret;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.noregret.Mapper.*;
import com.noregret.Pojo.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ServerSocket;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;

@Component
public class ProcessMsg {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RequestMapper requestMapper;
    @Autowired
    private FriendMapper friendMapper;
    @Autowired
    private MessageMapper1 messageMapper1;
    @Autowired
    private GroupMapper groupMapper;
    @Autowired
    private MessageMapper2 messageMapper2;
    @Autowired
    private FileMapper fileMapper;
    @Autowired
    private FileRequestMapper fileRequestMapper;

    public static HashMap<String, ChannelHandlerContext> online1 = new HashMap<>(); //储存当前在线用户
    public static HashMap<ChannelHandlerContext, String> online2 = new HashMap<>();
    public static HashMap<String, String> privateChatting = new HashMap<>(); //记录私聊
    public static HashMap<String, String> groupChatting = new HashMap<>(); //记录群聊

    public static ChannelHandlerContext channel;

    public void init(ChannelHandlerContext ctx) {
        channel = ctx;
    }

    public void send(ObjectNode node, ChannelHandlerContext ctx) {
        byte[] bytes = node.toString().getBytes();
        int length = bytes.length;
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        buf.writeInt(length);
        buf.writeBytes(bytes);
        ctx.writeAndFlush(buf);
    }

    public void sendResponse(String response) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode msg = mapper.readTree(response);
        String type = msg.get("type").asText();
        if (String.valueOf(MsgType.MSG_LOGIN).equals(type)) {
            String username = msg.get("username").asText();
            String password = msg.get("password").asText();
            User user = userMapper.getUser(username);
            ObjectNode node = mapper.createObjectNode();
            node.put("type", String.valueOf(MsgType.MSG_LOGIN));
            if (user == null) {
                node.put("code", 300); //用户不存在
            } else {
                if (password.equals(user.getPassword())) {
                    if (!isExist(username)) {
                        node.put("code", 100); //登录成功
                        online1.put(username, channel);
                        online2.put(channel, username);
                    } else {
                        node.put("code", 400); //不能重复登录
                    }
                } else {
                    node.put("code", 200); //密码错误
                }
            }
            send(node, channel);
        } else if (String.valueOf(MsgType.MSG_REGISTER).equals(type)) {
            String username = msg.get("username").asText();
            String password = msg.get("password").asText();
            User user = userMapper.getUser(username);
            ObjectNode node = mapper.createObjectNode();
            node.put("type", String.valueOf(MsgType.MSG_REGISTER));
            if (user == null) {
                userMapper.insertUser(username, password);
                node.put("code", 100); //注册成功
            } else {
                node.put("code", 200); //用户名已存在
            }
            send(node, channel);
        } else if (String.valueOf(MsgType.MSG_FIND).equals(type)) {
            String username = msg.get("username").asText();
            String password = msg.get("password").asText();
            userMapper.updatePassword(username, password);
        } else if (String.valueOf(MsgType.MSG_FRIEND_REQUEST).equals(type)) {
            String username = msg.get("username").asText();
            String friendName = msg.get("friendName").asText();
            User user = userMapper.getUser(friendName);
            ObjectNode node = mapper.createObjectNode();
            node.put("type", String.valueOf(MsgType.MSG_FRIEND_REQUEST));
            if (user == null) {
                node.put("code", 200); //不存在该用户
            } else {
                if (friendMapper.selectFriend2(username, friendName) == null) {
                    if(requestMapper.countRequest(username,friendName,1) == 0) {
                        node.put("code", 100); //发送好友申请
                        requestMapper.insertRequest(username, friendName, 1);
                        if (isExist(friendName)) {
                            ObjectNode node1 = mapper.createObjectNode();
                            node1.put("type", String.valueOf(MsgType.MSG_NOTICE));
                            node1.put("username", username);
                            node1.put("code", 1);
                            ChannelHandlerContext ctx = online1.get(friendName);
                            send(node1, ctx);
                        }
                    }
                    else{
                        node.put("code", 400); //已发送过好友申请
                    }
                } else {
                    node.put("code", 300); //已添加该好友
                }
            }
            send(node, channel);
        } else if (String.valueOf(MsgType.MSG_HOME).equals(type)) {
            String username = msg.get("username").asText();
            ObjectNode node = mapper.createObjectNode();
            node.put("type", String.valueOf(MsgType.MSG_HOME));

            List<String> friends2 = messageMapper1.getFriends(username);
            String friends = mapper.writeValueAsString(friends2);
            node.put("friends", friends);

            List<String> fromUsers2 = requestMapper.selectRequest1(username);
            String fromUsers = mapper.writeValueAsString(fromUsers2);
            node.put("fromUsers", fromUsers);

            List<Request> requests2 = requestMapper.selectRequest2(username);
            String requests = mapper.writeValueAsString(requests2);
            node.put("requests", requests);

            send(node, channel);
        } else if (String.valueOf(MsgType.MSG_LIST_FRIEND_REQUEST).equals(type)) {
            String username = msg.get("username").asText();
            List<String> fromUsers1 = requestMapper.selectRequest1(username);
            String fromUsers = mapper.writeValueAsString(fromUsers1);
            ObjectNode node = mapper.createObjectNode();
            node.put("type", String.valueOf(MsgType.MSG_LIST_FRIEND_REQUEST));
            node.put("fromUsers", fromUsers);
            send(node, channel);
        } else if (String.valueOf(MsgType.MSG_FRIEND_RESPONSE).equals(type)) {
            String fromUser = msg.get("fromUser").asText();
            String toUser = msg.get("toUser").asText();
            int code = msg.get("code").asInt();
            if (code == 0) {
                friendMapper.insertFriendship(fromUser, toUser);
                friendMapper.insertFriendship(toUser, fromUser);
            }
            if(isExist(fromUser)){
                ObjectNode node1 = mapper.createObjectNode();
                node1.put("type", String.valueOf(MsgType.MSG_NOTICE));
                node1.put("friend",toUser);
                node1.put("choice",code);
                node1.put("code",5);
                ChannelHandlerContext ctx = online1.get(fromUser);
                send(node1, ctx);
            }
            requestMapper.deleteRequest(fromUser, toUser, 1);
        } else if (String.valueOf(MsgType.MSG_LIST_FRIEND).equals(type)) {
            String username = msg.get("username").asText();
            List<String> friends = friendMapper.selectFriend(username);
            ObjectNode node = mapper.createObjectNode();
            node.put("type", String.valueOf(MsgType.MSG_LIST_FRIEND));
            List<theFriend> online = new ArrayList<>();
            for (String friend : friends) {
                if (online1.containsKey(friend)) {
                    online.add(new theFriend(friend, 1));//在线
                } else {
                    online.add(new theFriend(friend, 0)); //不在线
                }
            }
            String online1 = mapper.writeValueAsString(online);
            node.put("online", online1);
            send(node, channel);
        } else if (String.valueOf(MsgType.MSG_DELETE_FRIEND).equals(type)) {
            String username = msg.get("username").asText();
            String friendName = msg.get("friendName").asText();
            friendMapper.deleteFriend(username, friendName);
            friendMapper.deleteFriend(friendName, username);
        } else if (String.valueOf(MsgType.MSG_OFFLINE).equals(type)) {
            String username = msg.get("username").asText();
            ChannelHandlerContext ctx = online1.get(username);
            online1.remove(username);
            online2.remove(ctx);
        } else if (String.valueOf(MsgType.MSG_FRIEND_MESSAGE).equals(type)) {
            String username = msg.get("username").asText();
            String friendName = msg.get("friendName").asText();
            ObjectNode node = mapper.createObjectNode();
            node.put("type", String.valueOf(MsgType.MSG_FRIEND_MESSAGE));
            privateChatting.put(username, friendName);
            List<Message> messages2 = messageMapper1.privateChat(username, friendName);
            String messages = mapper.writeValueAsString(messages2);
            node.put("messages", messages);
            messageMapper1.update(username,friendName);
            send(node, channel);
        } else if (String.valueOf(MsgType.MSG_PRIVATE_CHAT).equals(type)) {
            String content = msg.get("content").asText();
            String fromUser = msg.get("fromUser").asText();
            String toUser = msg.get("toUser").asText();
            String time = msg.get("time").asText();
            Timestamp time2 = Timestamp.valueOf(time);
            privateChatting.put(fromUser,toUser);
            if ("q".equals(content)) {
                privateChatting.remove(fromUser);
                return;
            }

            int status = friendMapper.selectStatus(toUser, fromUser);
            ObjectNode node2 = mapper.createObjectNode();
            node2.put("type",String.valueOf(MsgType.MSG_GET_STATUS));
            node2.put("status",status);
            send(node2, channel);
            if(status == 1){
                return;
            }

            ObjectNode node = mapper.createObjectNode();
            node.put("type", String.valueOf(MsgType.MSG_PRIVATE_CHAT));
            node.put("content", content);
            node.put("time", time);
            node.put("fromUser", fromUser);

            if (messageMapper1.count(fromUser, toUser) >= 500) {
                messageMapper1.delete(fromUser, toUser);
            }

            if (!isExist(toUser)) {
                messageMapper1.insert(fromUser, toUser, content, time2, "unread");
            } else {
                ChannelHandlerContext ctx = online1.get(toUser);
                if (privateChatting.containsKey(toUser) && privateChatting.get(toUser).equals(fromUser)) {
                    send(node, ctx);
                    messageMapper1.insert(fromUser, toUser, content, time2, "read");
                } else {
                    ObjectNode node1 = mapper.createObjectNode();
                    node1.put("type", String.valueOf(MsgType.MSG_NOTICE));
                    node1.put("fromUser", fromUser);
                    node1.put("code", 3);
                    send(node1, ctx);
                    messageMapper1.insert(fromUser, toUser, content, time2, "unread");
                }
            }
        } else if (String.valueOf(MsgType.MSG_SEND_FILE).equals(type)) {
            String to = msg.get("to").asText();
            String from = msg.get("from").asText();
            String time = msg.get("time").asText();
            Timestamp time2 = Timestamp.valueOf(time.substring(0,19));
            String filename = msg.get("filename").asText();
            fileMapper.insert(filename,time2);
            int fileID = fileMapper.selectID(filename,time2);
            fileRequestMapper.insert(fileID,from,to,2,filename);
            int fromPort = getFreePort();
            new RecvFileThread(fromPort,fileID).start();

            ObjectNode node = mapper.createObjectNode();
            node.put("type", String.valueOf(MsgType.MSG_SEND_FILE));
            node.put("port",fromPort);
            send(node, channel);
        }else if(String.valueOf(MsgType.MSG_RECEIVE_FILE).equals(type)){
            String username = msg.get("username").asText();
            List<FileRequest> fileRequests = fileRequestMapper.findByUsername(username);
            String fileRequests2 = mapper.writeValueAsString(fileRequests);
            ObjectNode node = mapper.createObjectNode();
            node.put("type", String.valueOf(MsgType.MSG_RECEIVE_FILE));
            node.put("fileRequests", fileRequests2);
            send(node, channel);
        }
        else if (String.valueOf(MsgType.MSG_FRIEND_MENU).equals(type)) {
            String username = msg.get("username").asText();
            String friendName = msg.get("friendName").asText();
            int status = friendMapper.selectStatus(username, friendName);
            ObjectNode node = mapper.createObjectNode();
            node.put("type", String.valueOf(MsgType.MSG_FRIEND_MENU));
            node.put("status", status);
            send(node, channel);
        } else if (String.valueOf(MsgType.MSG_BLOCK).equals(type)) {
            String username = msg.get("username").asText();
            String friendName = msg.get("friendName").asText();
            friendMapper.modifyStatus(username, friendName, 1);
            if(isExist(friendName) && privateChatting.containsKey(friendName) && privateChatting.get(friendName).equals(username)){
                ObjectNode node = mapper.createObjectNode();
                node.put("type", String.valueOf(MsgType.MSG_NOTICE));
                node.put("username",username);
                node.put("code",6);
                ChannelHandlerContext ctx = online1.get(friendName);
                send(node, ctx);
            }
        } else if (String.valueOf(MsgType.MSG_UNBLOCK).equals(type)) {
            String username = msg.get("username").asText();
            String friendName = msg.get("friendName").asText();
            friendMapper.modifyStatus(username, friendName, 0);
            if(isExist(friendName) && privateChatting.containsKey(friendName) && privateChatting.get(friendName).equals(username)){
                ObjectNode node = mapper.createObjectNode();
                node.put("type", String.valueOf(MsgType.MSG_NOTICE));
                node.put("username",username);
                node.put("code",7);
                ChannelHandlerContext ctx = online1.get(friendName);
                send(node, ctx);
            }
        } else if (String.valueOf(MsgType.MSG_CREATE_GROUP).equals(type)) {
            String groupName = msg.get("groupName").asText();
            String member = msg.get("member").asText();

            ObjectNode node = mapper.createObjectNode();
            node.put("type", String.valueOf(MsgType.MSG_CREATE_GROUP));
            if (groupMapper.getGroup(groupName) != null) {
                node.put("code", 100); //名称已被占用
            } else {
                node.put("code", 200); //建群成功
                groupMapper.insert(groupName, member, 1);
            }
            send(node, channel);
        } else if (String.valueOf(MsgType.MSG_GROUP_REQUEST).equals(type)) {
            String to = msg.get("groupName").asText();
            String from = msg.get("member").asText();
            ObjectNode node = mapper.createObjectNode();
            node.put("type", String.valueOf(MsgType.MSG_GROUP_REQUEST));
            if (groupMapper.getGroup(to) == null) {
                node.put("code", 100); //群组不存在
            } else {
                if (groupMapper.getCount(to, from) == 0) {
                    if (requestMapper.countRequest(from, to, 2) == 0) {
                        requestMapper.insertRequest(from, to, 2);
                        node.put("code", 200); //发送加群申请
                        List<Member> members = groupMapper.getMembers(to);
                        for (Member member : members) {
                            if (member.getRole() == 1 || member.getRole() == 2) {
                                if (isExist(member.getMember())) {
                                    ObjectNode node1 = mapper.createObjectNode();
                                    node1.put("type", String.valueOf(MsgType.MSG_NOTICE));
                                    node1.put("from", from);
                                    node1.put("to", to);
                                    node1.put("code", 2);
                                    ChannelHandlerContext ctx = online1.get(member.getMember());
                                    send(node1, ctx);
                                }
                            }
                        }
                    } else {
                        node.put("code", 300); //已发送过加群申请
                    }
                }else {
                    node.put("code", 400); //已加入该群组
                }
            }
            send(node, channel);
        } else if (String.valueOf(MsgType.MSG_LIST_GROUP_REQUEST).equals(type)) {
            ObjectNode node = mapper.createObjectNode();
            node.put("type", String.valueOf(MsgType.MSG_LIST_GROUP_REQUEST));
            String username = msg.get("username").asText();
            List<Request> requests2 = requestMapper.selectRequest2(username);
            String requests = mapper.writeValueAsString(requests2);
            node.put("requests", requests);
            send(node, channel);
        } else if (String.valueOf(MsgType.MSG_GROUP_RESPONSE).equals(type)) {
            String groupName = msg.get("groupName").asText();
            String from = msg.get("fromUser").asText();
            int code = msg.get("code").asInt();
            if (code == 0) {
                groupMapper.insert(groupName, from, 3);
            }
            requestMapper.deleteRequest(from, groupName, 2);
        } else if (String.valueOf(MsgType.MSG_LIST_GROUP).equals(type)) {
            String username = msg.get("username").asText();
            List<String> groups2 = groupMapper.getGroups(username);
            ObjectNode node = mapper.createObjectNode();
            node.put("type", String.valueOf(MsgType.MSG_LIST_GROUP));
            String groups = mapper.writeValueAsString(groups2);
            node.put("groups", groups);
            send(node, channel);
        }else if(String.valueOf(MsgType.MSG_MEMBER_ROLE).equals(type)){
            String username = msg.get("username").asText();
            String groupName = msg.get("groupName").asText();
            int role = groupMapper.getRole(groupName,username);
            ObjectNode node = mapper.createObjectNode();
            node.put("type", String.valueOf(MsgType.MSG_MEMBER_ROLE));
            node.put("role", role);
            send(node, channel);
        }
        else if (String.valueOf(MsgType.MSG_GROUP_MEMBER).equals(type)) {
            String groupName = msg.get("groupName").asText();
            List<Member> members = groupMapper.getMembers(groupName);
            ObjectNode node = mapper.createObjectNode();
            String member2 = mapper.writeValueAsString(members);
            node.put("members", member2);
            node.put("type", String.valueOf(MsgType.MSG_GROUP_MEMBER));
            send(node, channel);
        } else if (String.valueOf(MsgType.MSG_QUIT_GROUP).equals(type)) {
            String username = msg.get("username").asText();
            String groupName = msg.get("groupName").asText();
            groupMapper.deleteMember(groupName, username);
        } else if (String.valueOf(MsgType.MSG_BREAK_GROUP).equals(type)) {
            String groupName = msg.get("groupName").asText();
            groupMapper.deleteGroup(groupName);
            requestMapper.breakGroup(groupName);
            messageMapper2.breakGroup(groupName);
        } else if (String.valueOf(MsgType.MSG_REMOVE_MEMBER).equals(type)) {
            String groupName = msg.get("groupName").asText();
            String member = msg.get("member").asText();
            groupMapper.deleteMember(groupName, member);
        } else if (String.valueOf(MsgType.MSG_ADD_MANAGER).equals(type)) {
            String groupName = msg.get("groupName").asText();
            String member = msg.get("member").asText();
            groupMapper.modifyManager(groupName, member, 2);
        } else if (String.valueOf(MsgType.MSG_REMOVE_MANAGER).equals(type)) {
            String groupName = msg.get("groupName").asText();
            String member = msg.get("member").asText();
            groupMapper.modifyManager(groupName, member, 3);
        } else if (String.valueOf(MsgType.MSG_GROUP_MESSAGE).equals(type)) {
            String groupName = msg.get("groupName").asText();
            ObjectNode node = mapper.createObjectNode();
            node.put("type", String.valueOf(MsgType.MSG_GROUP_MESSAGE));
            List<Message> messages2 = messageMapper2.groupChat(groupName);
            String messages = mapper.writeValueAsString(messages2);
            node.put("messages", messages);
            send(node, channel);
        } else if (String.valueOf(MsgType.MSG_GROUP_CHAT).equals(type)) {
            String from = msg.get("from").asText();
            String content = msg.get("content").asText();
            String to = msg.get("to").asText();
            String time2 = msg.get("time").asText();
            Timestamp time = Timestamp.valueOf(time2);
            groupChatting.put(from,to);
            if ("q".equals(content)) {
                groupChatting.remove(from);
                return;
            }

            int status = groupMapper.getStatus(to, from);
            ObjectNode node2 = mapper.createObjectNode();
            node2.put("type", String.valueOf(MsgType.MSG_GET_STATUS));
            node2.put("status", status);
            send(node2, channel);
            if(status == 1){
                return;
            }

            ObjectNode node = mapper.createObjectNode();
            node.put("type", String.valueOf(MsgType.MSG_GROUP_CHAT));
            node.put("from", from);
            node.put("to", to);
            node.put("content", content);
            node.put("time", time.toString());

            if (messageMapper2.count(to) >= 500) {
                messageMapper2.delete(to);
            }


            messageMapper2.insert(from, to, content, time, null);

            //遍历群友并转发
            List<String> members = groupMapper.getMemberNames(to);
            if (!members.isEmpty()) {
                for (String member : members) {
                    if (member.equals(from)) {
                        continue;
                    }
                    if (isExist(member)) {
                        ChannelHandlerContext ctx = online1.get(member);
                        if (groupChatting.containsKey(member) && groupChatting.get(member).equals(to)) {
                            send(node, ctx);
                        } else {
                            ObjectNode node1 = mapper.createObjectNode();
                            node1.put("group", to);
                            node1.put("type", String.valueOf(MsgType.MSG_NOTICE));
                            node1.put("code", 4);
                            send(node1, ctx);
                        }
                    }
                }
            }
        } else if (String.valueOf(MsgType.MSG_BLOCK_MEMBER).equals(type)) {
            String groupName = msg.get("groupName").asText();
            String member = msg.get("member").asText();
            groupMapper.modifyStatus(groupName, member, 1);
            if(isExist(member) && groupChatting.containsKey(member) && groupChatting.get(member).equals(groupName)){
                ObjectNode node = mapper.createObjectNode();
                node.put("type", String.valueOf(MsgType.MSG_NOTICE));
                node.put("code", 8);
                ChannelHandlerContext ctx = online1.get(member);
                send(node, ctx);
            }
        }else if(String.valueOf(MsgType.MSG_UNBLOCK_MEMBER).equals(type)){
            String groupName = msg.get("groupName").asText();
            String member = msg.get("member").asText();
            groupMapper.modifyStatus(groupName, member, 0);
            if(isExist(member) && groupChatting.containsKey(member) && groupChatting.get(member).equals(groupName)){
                ObjectNode node = mapper.createObjectNode();
                node.put("type", String.valueOf(MsgType.MSG_NOTICE));
                node.put("code", 9);
                ChannelHandlerContext ctx = online1.get(member);
                send(node, ctx);
            }
        }else if(String.valueOf(MsgType.MSG_DELETE_USER).equals(type)){
            String username = msg.get("username").asText();
            userMapper.deleteUser(username);
            requestMapper.deleteUser(username);
            messageMapper1.deleteUser(username);
            messageMapper2.deleteUser(username);
            friendMapper.deleteUser(username);
            groupMapper.deleteUser(username);
            ChannelHandlerContext ctx = online1.get(username);
            online1.remove(username);
            online2.remove(ctx);
        }else if(String.valueOf(MsgType.MSG_SEND_GROUP_FILE).equals(type)){
            String from = msg.get("from").asText();
            String to = msg.get("to").asText();
            List<String> members = groupMapper.getMemberNames(to);
            int code = 1;
            int fromPort = getFreePort();
            if (!members.isEmpty()) {
                for (String member : members) {
                    if(from.equals(member)){
                        continue;
                    }
                    if(isExist(member)){
                        code = 0;
                        int toPort = getFreePort();
                        new RecvFileThread(fromPort,toPort).start();

                        //发送给接收者
                        ObjectNode node1 = mapper.createObjectNode();
                        node1.put("from",from);
                        node1.put("to",to);
                        node1.put("type", String.valueOf(MsgType.MSG_RECEIVE_FILE));
                        node1.put("port", toPort);
                        node1.put("code",2); //群聊接收文件
                        ChannelHandlerContext ctx = online1.get(member);
                        send(node1, ctx);
                    }
                }
            }
            if(code == 0){
                ObjectNode node2 = mapper.createObjectNode();
                node2.put("type", String.valueOf(MsgType.MSG_SEND_GROUP_FILE));
                node2.put("port", fromPort);
                node2.put("code", 0); //告诉发送者有人在线
                send(node2, channel);
            }
            if(code == 1){
                ObjectNode node2 = mapper.createObjectNode();
                node2.put("type", String.valueOf(MsgType.MSG_SEND_GROUP_FILE));
                node2.put("code", 1); //告诉发送者无人在线
                send(node2, channel);
            }
        }else if(String.valueOf(MsgType.MSG_GET_STATUS).equals(type)){
            int code = msg.get("code").asInt();
            if(code == 1) {
                String username = msg.get("username").asText();
                String friendName = msg.get("friendName").asText();

                int status = friendMapper.selectStatus(friendName, username);
                ObjectNode node = mapper.createObjectNode();
                node.put("type", String.valueOf(MsgType.MSG_GET_STATUS));
                node.put("status", status);
                send(node, channel);
            }
            else if(code == 2) {
                String username = msg.get("username").asText();
                String groupName = msg.get("groupName").asText();

                int status = groupMapper.getStatus(groupName, username);
                ObjectNode node = mapper.createObjectNode();
                node.put("type", String.valueOf(MsgType.MSG_GET_STATUS));
                node.put("status", status);
                send(node, channel);
            }
        }else if(String.valueOf(MsgType.MSG_UPLOAD_FILE).equals(type)){
            int fileID = msg.get("fileID").asInt();
            int toPort = getFreePort();
            new SendFileThread(toPort,fileID).start();

            ObjectNode node = mapper.createObjectNode();
            node.put("type", String.valueOf(MsgType.MSG_UPLOAD_FILE));
            node.put("port", toPort);
            send(node, channel);
            fileRequestMapper.updateStatus(fileID);
        }
    }

    //判断该用户是否在线
    public static boolean isExist(ChannelHandlerContext ctx) {
        return online2.containsKey(ctx);
    }

    public static boolean isExist(String username) {
        return online1.containsKey(username);
    }

    public static void remove(ChannelHandlerContext ctx) {
        String username = online2.get(ctx);
        online1.remove(username);
        online2.remove(ctx);
        privateChatting.remove(username);
        groupChatting.remove(username);
    }

    public static int getFreePort(){
        try (ServerSocket serverSocket = new ServerSocket(0)){
            return serverSocket.getLocalPort();
        } catch (IOException e) {
            throw new IllegalStateException("cannot find available port:" + e.getMessage());
        }
    }
}
