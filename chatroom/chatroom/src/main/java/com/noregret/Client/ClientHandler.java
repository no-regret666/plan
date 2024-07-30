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
        else if(String.valueOf(MsgType.MSG_FRIEND_REQUEST).equals(type)){
            int code = node.get("code").asInt();
            queue.put(code);
        }
        else if(String.valueOf(MsgType.MSG_LIST_FRIEND_REQUEST).equals(type)){
            String fromUsers = node.get("fromUsers").asText();
            queue2.put(fromUsers);
        }
    }
}
