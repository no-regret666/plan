package com.noregret;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.noregret.pojo.Request;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.FileOutputStream;
import java.util.Base64;
import java.util.concurrent.SynchronousQueue;

public class ClientHandler extends SimpleChannelInboundHandler<String> {
    public static SynchronousQueue<Integer> queue = new SynchronousQueue<>();
    public static SynchronousQueue<Object> queue2 = new SynchronousQueue<>();
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s){
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //System.out.println("Client received: " + msg);
        if(msg instanceof Request request){
            if(request.getType() == 1){
                System.out.println(request.getFrom() + "申请添加为好友!");
            }
            else if(request.getType() == 2){
                System.out.println(request.getFrom() + "申请加入" + request.getTo() + "群组!");
            }
        }else if(msg instanceof String response) {
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
            } else if (String.valueOf(MsgType.MSG_LIST_FRIEND_REQUEST).equals(type)) {
                String fromUsers = node.get("fromUsers").asText();
                queue2.put(fromUsers);
            } else if (String.valueOf(MsgType.MSG_LIST_FRIEND).equals(type)) {
                String friends = node.get("online").asText();
                queue2.put(friends);
            } else if (String.valueOf(MsgType.MSG_PRIVATE_CHAT).equals(type)) {
                int code = node.get("code").asInt();
                queue.put(code);
                if (code == 200) {
                    String messages = node.get("messages").asText();
                    queue2.put(messages);
                }
            } else if (String.valueOf(MsgType.MSG_SEND_MESSAGE1).equals(type)) {
                String fromUser = node.get("fromUser").asText();
                String content = node.get("content").asText();
                String time = node.get("time").asText();
                System.out.println(time.substring(0, 19) + " " + fromUser + ":" + content);
            } else if (String.valueOf(MsgType.MSG_CREATE_GROUP).equals(type)) {
                int code = node.get("code").asInt();
                queue.put(code);
            } else if (String.valueOf(MsgType.MSG_GROUP_REQUEST).equals(type)) {
                int code = node.get("code").asInt();
                queue.put(code);
            } else if (String.valueOf(MsgType.MSG_LIST_GROUP).equals(type)) {
                String groups = node.get("groups").asText();
                queue2.put(groups);
            } else if (String.valueOf(MsgType.MSG_GROUP_MEMBER).equals(type)) {
                String members = node.get("members").asText();
                queue2.put(members);
            } else if (String.valueOf(MsgType.MSG_LIST_GROUP_REQUEST).equals(type)) {
                String requests = node.get("requests").asText();
                queue2.put(requests);
            } else if (String.valueOf(MsgType.MSG_GROUP_CHAT).equals(type)) {
                String messages = node.get("messages").asText();
                queue2.put(messages);
            } else if (String.valueOf(MsgType.MSG_SEND_MESSAGE2).equals(type)) {
                String content = node.get("content").asText();
                String time = node.get("time").asText();
                String from = node.get("from").asText();
                System.out.println(time.substring(0, 19) + " " + from + ":" + content);
            }else if(String.valueOf(MsgType.MSG_SEND_FILE).equals(type)){
                String file = node.get("file").asText();
                byte[] base64File = Base64.getDecoder().decode(file);
                try (FileOutputStream fos = new FileOutputStream("received_file")){
                    fos.write(base64File);
                }
            }
        }
    }
}