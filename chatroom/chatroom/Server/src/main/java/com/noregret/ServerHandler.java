package com.noregret;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.stream.ChunkedFile;
import io.netty.handler.stream.ChunkedInput;
import io.netty.handler.stream.ChunkedNioFile;
import io.netty.handler.stream.ChunkedNioStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

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
        }else if(msg instanceof ByteBuf byteBuf){
//            ByteBuffer byteBuffer = byteBuf.nioBuffer();
//            File file = new File("received_file.txt");
//            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
//            FileChannel fileChannel = randomAccessFile.getChannel();
//            while(byteBuffer.hasRemaining()){
//                fileChannel.position(file.length());
//                int length = fileChannel.write(byteBuffer);
//                if(length == 0){
//                    break;
//                }
//            }
//            byteBuf.release();
//            fileChannel.close();
//            randomAccessFile.close();
            String from = ProcessMsg.online2.get(ctx);
            ChannelHandlerContext to = ProcessMsg.transferFile.get(from);
            to.writeAndFlush(byteBuf);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (ProcessMsg.isExist(ctx)) {
            ProcessMsg.remove(ctx);
        }
    }
}
