package com.noregret.Server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.noregret.MsgType;
import com.noregret.Server.Mapper.FriendMapper;
import com.noregret.Server.Mapper.MessageMapper;
import com.noregret.Server.Mapper.RequestMapper;
import com.noregret.Server.Mapper.UserMapper;
import com.noregret.Server.pojo.User;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;

@Component
public class ProcessMsg {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RequestMapper requestMapper;
    @Autowired
    private FriendMapper friendMapper;
    @Autowired
    private MessageMapper messageMapper;

    private static HashMap<String, ChannelHandlerContext> online1 = new HashMap<>(); //储存当前在线用户
    private static HashMap<ChannelHandlerContext,String> online2 = new HashMap<>();

    public void sendResponse(String response, ChannelHandlerContext channel) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode msg = mapper.readTree(response);
        String type = msg.get("type").asText();
        boolean login = false;
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
                    node.put("code", 100); //登录成功
                    online1.put(username,channel);
                    online2.put(channel,username);
                    login = true;
                } else {
                    node.put("code", 200); //密码错误
                }
            }
            //登录回复消息
            String recvMsg = node.toString();
            channel.writeAndFlush(recvMsg);
        }
        else if (String.valueOf(MsgType.MSG_REGISTER).equals(type)) {
            String username = msg.get("username").asText();
            String password = msg.get("password").asText();
            User user = userMapper.getUser(username);
            ObjectNode node = mapper.createObjectNode();
            node.put("type", String.valueOf(MsgType.MSG_REGISTER));
            if (user == null) {
                userMapper.insertUser(username, password);
                node.put("code", 100); //注册成功
            }
            else{
                node.put("code", 200); //用户名已存在
            }
            String recvMsg = node.toString();
            channel.writeAndFlush(recvMsg);
        }
        else if(String.valueOf(MsgType.MSG_FRIEND_REQUEST).equals(type)){
            String username = msg.get("username").asText();
            String friendName = msg.get("friendName").asText();
            User user = userMapper.getUser(username);
            ObjectNode node = mapper.createObjectNode();
            node.put("type", String.valueOf(MsgType.MSG_FRIEND_REQUEST));
            if (user == null) {
                node.put("code", 200); //不存在该用户
            }
            else{
                if(friendMapper.selectFriend2(username,friendName) == null) {
                    node.put("code", 100); //发送好友申请
                    requestMapper.insertRequest(username, friendName);
                }else{
                    node.put("code", 300); //已添加该好友
                }
            }
            String recvMsg = node.toString();
            channel.writeAndFlush(recvMsg);
        }
        else if(String.valueOf(MsgType.MSG_LIST_FRIEND_REQUEST).equals(type)){
            String username = msg.get("username").asText();
            List<String> fromUsers1 = requestMapper.selectRequest(username);
            ObjectMapper objectMapper = new ObjectMapper();
            String fromUsers = objectMapper.writeValueAsString(fromUsers1);
            ObjectNode node = objectMapper.createObjectNode();
            node.put("type", String.valueOf(MsgType.MSG_LIST_FRIEND_REQUEST));
            node.put("fromUsers", fromUsers);
            String recvMsg = node.toString();
            channel.writeAndFlush(recvMsg);
        }
        else if(String.valueOf(MsgType.MSG_FRIEND_RESPONSE).equals(type)){
            String fromUser = msg.get("fromUser").asText();
            String toUser = msg.get("toUser").asText();
            int code = msg.get("code").asInt();
            if(code == 0){
                friendMapper.insertFriendship(fromUser, toUser);
                friendMapper.insertFriendship(toUser, fromUser);
            }
            requestMapper.deleteRequest(fromUser,toUser);
        }
        else if(String.valueOf(MsgType.MSG_LIST_FRIEND).equals(type)){
            String username = msg.get("username").asText();
            List<String> friends1 = friendMapper.selectFriend(username);
            ObjectMapper objectMapper = new ObjectMapper();
            String friends = objectMapper.writeValueAsString(friends1);
            ObjectNode node = objectMapper.createObjectNode();
            node.put("type", String.valueOf(MsgType.MSG_LIST_FRIEND));
            node.put("friends", friends);
            String recvMsg = node.toString();
            channel.writeAndFlush(recvMsg);
        }
        else if(String.valueOf(MsgType.MSG_DELETE_FRIEND).equals(type)){
            String username = msg.get("username").asText();
            String friendName = msg.get("friendName").asText();
            friendMapper.deleteFriend(username,friendName);
            friendMapper.deleteFriend(friendName,username);
        }
        else if(String.valueOf(MsgType.MSG_OFFLINE).equals(type)){
            String username = msg.get("username").asText();
            ChannelHandlerContext ctx = online1.get(username);
            online1.remove(username);
            online2.remove(ctx);
        }
        else if(String.valueOf(MsgType.MSG_PRIVATE_CHAT).equals(type)){
            String username = msg.get("username").asText();
            String friendName = msg.get("friendName").asText();
            ObjectNode node = mapper.createObjectNode();
            node.put("type", String.valueOf(MsgType.MSG_PRIVATE_CHAT));
            if(friendMapper.selectFriend2(username,friendName) == null) {
                node.put("code", 100); //不是好友
            }
            else{
                if(isExist(friendName)){
                    node.put("code", 200); //好友在线
                }
                else{
                    node.put("code", 300); //好友不在线
                }
            }
            String recvMsg = node.toString();
            channel.writeAndFlush(recvMsg);
        }
        else if(String.valueOf(MsgType.MSG_SAVE_MESSAGE).equals(type)){
            String fromUser = msg.get("fromUser").asText();
            String toUser = msg.get("toUser").asText();
            String content = msg.get("content").asText();
            String time = msg.get("time").asText();
            Timestamp time2 = Timestamp.valueOf(time);
            int code = msg.get("code").asInt();
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode node = objectMapper.createObjectNode();
            if(code == 200){
                node.put("content", content);
                node.put("time",time);
                node.put("type", String.valueOf(MsgType.MSG_SAVE_MESSAGE));
                node.put("fromUser", fromUser);
                ChannelHandlerContext ctx = online1.get(toUser);
                String recvMsg = node.toString();
                ctx.writeAndFlush(recvMsg);
                messageMapper.insert(fromUser,toUser,content,time2,"read");
            }
            else if(code == 300){
                messageMapper.insert(fromUser,toUser,content,time2,"unread");
            }
        }
    }

    //判断该用户是否在线
    public static boolean isExist(ChannelHandlerContext ctx){
        return online2.containsKey(ctx);
    }

    public static boolean isExist(String username){
        return online1.containsKey(username);
    }

    public static void remove(ChannelHandlerContext ctx){
        online2.remove(ctx);
    }
}
