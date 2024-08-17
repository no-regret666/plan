package com.noregret;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.SynchronousQueue;

public class ClientHandler extends ChannelInboundHandlerAdapter {
    public static SynchronousQueue<Integer> queue = new SynchronousQueue<>();
    public static SynchronousQueue<Object> queue2 = new SynchronousQueue<>();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // System.out.println("Client received: " + msg);
        if (msg instanceof String response) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(response);
            String type = node.get("type").asText();
            if (String.valueOf(MsgType.MSG_LOGIN).equals(type)) {
                int code = node.get("code").asInt();
                queue.put(code);
            } else if (String.valueOf(MsgType.MSG_REGISTER).equals(type)) {
                int code = node.get("code").asInt();
                queue.put(code);
            } else if (String.valueOf(MsgType.MSG_HOME).equals(type)) {
                String fromUsers = node.get("fromUsers").asText();
                queue2.put(fromUsers);
                String requests = node.get("requests").asText();
                queue2.put(requests);
                String friends = node.get("friends").asText();
                queue2.put(friends);
            } else if (String.valueOf(MsgType.MSG_FRIEND_REQUEST).equals(type)) {
                int code = node.get("code").asInt();
                queue.put(code);
            } else if (String.valueOf(MsgType.MSG_NOTICE).equals(type)) {
                int code = node.get("code").asInt();
                if (code == 1) {
                    String username = node.get("username").asText();
                    System.out.println(Utils.getColoredString(33, 1, username + " 申请添加为好友!"));
                } else if (code == 2) {
                    String from = node.get("from").asText();
                    String to = node.get("to").asText();
                    System.out.println(Utils.getColoredString(33, 1, from + " 申请加入 " + to + " 群组!"));
                } else if (code == 3) {
                    String fromUser = node.get("fromUser").asText();
                    System.out.println(Utils.getColoredString(33, 1, fromUser + " 发来了新消息!"));
                } else if (code == 4) {
                    String group = node.get("group").asText();
                    System.out.println(Utils.getColoredString(33, 1, group + " 有新消息!"));
                } else if (code == 5) {
                    String friend = node.get("friend").asText();
                    int choice = node.get("choice").asInt();
                    if (choice == 0) {
                        System.out.println(Utils.getColoredString(33, 1, friend + " 同意添加您为好友!"));
                    } else if (choice == 1) {
                        System.out.println(Utils.getColoredString(33, 1, friend + " 拒绝添加您为好友!"));
                    }
                } else if (code == 6) {
                    System.out.println(Utils.getColoredString(33, 1, "对方已屏蔽你!"));
                } else if (code == 7) {
                    System.out.println(Utils.getColoredString(33, 1, "对方解除了对你的屏蔽!"));
                } else if (code == 8) {
                    System.out.println(Utils.getColoredString(33, 1, "你已被禁言!"));
                } else if (code == 9) {
                    System.out.println(Utils.getColoredString(33, 1, "你已被解除禁言!"));
                }
            } else if (String.valueOf(MsgType.MSG_LIST_FRIEND_REQUEST).equals(type)) {
                String fromUsers = node.get("fromUsers").asText();
                queue2.put(fromUsers);
            } else if (String.valueOf(MsgType.MSG_LIST_FRIEND).equals(type)) {
                String friends = node.get("online").asText();
                queue2.put(friends);
            } else if (String.valueOf(MsgType.MSG_FRIEND_MESSAGE).equals(type)) {
                String messages = node.get("messages").asText();
                queue2.put(messages);
            } else if (String.valueOf(MsgType.MSG_PRIVATE_CHAT).equals(type)) {
                String fromUser = node.get("fromUser").asText();
                String content = node.get("content").asText();
                String time = node.get("time").asText();
                System.out.println(time.substring(0, 19) + " " + fromUser + ":" + content);
            } else if (String.valueOf(MsgType.MSG_FRIEND_MENU).equals(type)) {
                int status = node.get("status").asInt();
                queue.put(status);
            } else if (String.valueOf(MsgType.MSG_CREATE_GROUP).equals(type)) {
                int code = node.get("code").asInt();
                queue.put(code);
            } else if (String.valueOf(MsgType.MSG_GROUP_REQUEST).equals(type)) {
                int code = node.get("code").asInt();
                queue.put(code);
            } else if (String.valueOf(MsgType.MSG_LIST_GROUP).equals(type)) {
                String groups = node.get("groups").asText();
                queue2.put(groups);
            } else if (String.valueOf(MsgType.MSG_MEMBER_ROLE).equals(type)) {
                int role = node.get("role").asInt();
                queue.put(role);
            } else if (String.valueOf(MsgType.MSG_GROUP_MEMBER).equals(type)) {
                String members = node.get("members").asText();
                queue2.put(members);
            } else if (String.valueOf(MsgType.MSG_LIST_GROUP_REQUEST).equals(type)) {
                String requests = node.get("requests").asText();
                queue2.put(requests);
            } else if (String.valueOf(MsgType.MSG_GROUP_MESSAGE).equals(type)) {
                String messages = node.get("messages").asText();
                queue2.put(messages);
            } else if (String.valueOf(MsgType.MSG_GROUP_CHAT).equals(type)) {
                String content = node.get("content").asText();
                String time = node.get("time").asText();
                String from = node.get("from").asText();
                System.out.println(time.substring(0, 19) + " " + from + ":" + content);
            } else if (String.valueOf(MsgType.MSG_SEND_FILE).equals(type) || String.valueOf(MsgType.MSG_SEND_GROUP_FILE).equals(type)) {
                int port = node.get("port").asInt();
                queue.put(port);
            }else if(String.valueOf(MsgType.MSG_UPLOAD_FILE).equals(type)){
                int port = node.get("port").asInt();
                queue.put(port);
            }
            else if (String.valueOf(MsgType.MSG_GET_STATUS).equals(type)) {
                int status = node.get("status").asInt();
                queue.put(status);
            } else if (String.valueOf(MsgType.MSG_RECEIVE_FILE).equals(type)) {
                String fileRequests = node.get("fileRequests").asText();
                queue2.put(fileRequests);
            }
        }
    }
}