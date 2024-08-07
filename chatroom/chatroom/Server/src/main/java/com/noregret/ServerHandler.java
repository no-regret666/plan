package com.noregret;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.RandomAccessFile;

@Component
@ChannelHandler.Sharable
@Slf4j
public class ServerHandler extends SimpleChannelInboundHandler<String> {
    @Autowired
    private ProcessMsg processMsg;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            System.out.println("Server Received: " + msg);
            processMsg.init(ctx);
            String response = (String) msg;
            processMsg.sendResponse(response);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (ProcessMsg.isExist(ctx)) {
            ProcessMsg.remove(ctx);
        }
    }
}
