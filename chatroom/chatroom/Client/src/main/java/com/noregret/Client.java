package com.noregret;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
public class Client {
    public void init(String host, int port) {
        //创建事件循环组
        NioEventLoopGroup group = new NioEventLoopGroup();

        try {
            //创建启动辅助类
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new LengthFieldBasedFrameDecoder(1024 * 1024, 0, 4, 0, 4));
                            pipeline.addLast(new StringEncoder());
                            pipeline.addLast(new StringDecoder());
                            pipeline.addLast(new ClientHandler());
                        }
                    });
            //连接服务器
            Channel channel = bootstrap.connect(host, port).sync().channel();
            channel.config().setOption(ChannelOption.SO_SNDBUF, 65535);
            SendService sendService = new SendService(channel);
            group.submit(() -> {
                try {
                    sendService.menu();
                } catch (InterruptedException | IOException e) {
                    throw new RuntimeException(e);
                }
            });
            channel.closeFuture().sync();

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            group.shutdownGracefully();
        }
    }
}
