package com.noregret.Server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.noregret.MsgType;
import com.noregret.Server.Mapper.RequestMapper;
import com.noregret.Server.Mapper.UserMapper;
import com.noregret.Server.pojo.User;
import io.netty.channel.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProcessMsg {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RequestMapper requestMapper;

    public void sendResponse(String response, Channel channel) throws Exception {
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
                node.put("code", 100); //发送好友申请
                requestMapper.insertRequest(username, friendName);
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
    }
}
