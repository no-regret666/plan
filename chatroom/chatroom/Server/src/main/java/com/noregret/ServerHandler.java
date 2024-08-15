package com.noregret;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@ChannelHandler.Sharable
@Slf4j
public class ServerHandler extends ChannelInboundHandlerAdapter {
    @Autowired
    private ProcessMsg processMsg;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println(msg);
        if (msg instanceof String response) {
            System.out.println("Server Received: " + msg);
            processMsg.init(ctx);
            processMsg.sendResponse(response);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (ProcessMsg.isExist(ctx)) {
            ProcessMsg.remove(ctx);
        }
    }
}
