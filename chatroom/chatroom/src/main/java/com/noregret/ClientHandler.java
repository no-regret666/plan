package com.noregret;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ClientHandler extends SimpleChannelInboundHandler<String> {
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

        }
    }
}
