package com.noregret.Client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.noregret.MsgType;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.SynchronousQueue;

public class ClientHandler extends SimpleChannelInboundHandler<String> {
    public static SynchronousQueue<Integer> queue = new SynchronousQueue<>();
    public static SynchronousQueue<Object> queue2 = new SynchronousQueue<>();
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("Client received: " + msg);
        String response = (String) msg;
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(response);
        String type = node.get("type").asText();
        if(String.valueOf(MsgType.MSG_LOGIN).equals(type)){
            int code = node.get("code").asInt();
            queue.put(code);
        }
        else if(String.valueOf(MsgType.MSG_REGISTER).equals(type)){
            int code = node.get("code").asInt();
            queue.put(code);
        }
        else if(String.valueOf(MsgType.MSG_FIND).equals(type)){

        }
        else if(String.valueOf(MsgType.MSG_FRIEND_REQUEST).equals(type)){
            int code = node.get("code").asInt();
            queue.put(code);
        }
        else if(String.valueOf(MsgType.MSG_LIST_FRIEND_REQUEST).equals(type)){
            String fromUsers = node.get("fromUsers").asText();
            queue2.put(fromUsers);
        }
        else if(String.valueOf(MsgType.MSG_LIST_FRIEND).equals(type)){
            String friends = node.get("friends").asText();
            queue2.put(friends);
        }
        else if(String.valueOf(MsgType.MSG_PRIVATE_CHAT).equals(type)){
            int code = node.get("code").asInt();
            queue.put(code);
            if(code == 200){
                String messages = node.get("messages").asText();
                queue2.put(messages);
            }
        }else if(String.valueOf(MsgType.MSG_SAVE_MESSAGE).equals(type)){
            String fromUser = node.get("fromUser").asText();
            String content = node.get("content").asText();
            String time = node.get("time").asText();
            System.out.println(fromUser + ":" + content + "  " + time);
        }else if(String.valueOf(MsgType.MSG_CREATE_GROUP).equals(type)){
            int code = node.get("code").asInt();
            queue.put(code);
        }
        else if(String.valueOf(MsgType.MSG_GROUP_REQUEST).equals(type)){
            int code = node.get("code").asInt();
            queue.put(code);
        }
        else if(String.valueOf(MsgType.MSG_LIST_GROUP).equals(type)){
            String groups = node.get("groups").asText();
            queue2.put(groups);
        }
        else if(String.valueOf(MsgType.MSG_GROUP_MEMBER).equals(type)){
            String members = node.get("members").asText();
            queue2.put(members);
        }
        else if(String.valueOf(MsgType.MSG_LIST_GROUP_REQUEST).equals(type)){
            String fromUsers = node.get("fromUsers").asText();
            queue2.put(fromUsers);
        }
    }
}
