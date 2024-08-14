package com.noregret;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.stream.ChunkedFile;

public class ChunkedFileHandler extends ChannelDuplexHandler {
    private final ChannelHandler chunkedWriteHandler;

    public ChunkedFileHandler(ChannelHandler chunkedWriteHandler) {
        this.chunkedWriteHandler = chunkedWriteHandler;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if(msg instanceof ChunkedFile){
            ctx.write(msg, promise).addListener(future -> {
                if(!future.isSuccess()){
                    promise.setFailure(future.cause());
                }
            });
        }else{
            super.write(ctx, msg, promise);
        }
    }
}
