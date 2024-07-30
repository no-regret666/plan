package com.noregret.Client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.springframework.stereotype.Component;

@Component
public class Client {
    public void init(String host,int port){
        //创建事件循环组
        NioEventLoopGroup group = new NioEventLoopGroup();

        try {
            //创建启动辅助类
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new StringDecoder());
                            pipeline.addLast(new StringEncoder());
                            pipeline.addLast(new ClientHandler());
                        }
                    });
            //连接服务器
            Channel channel = bootstrap.connect(host,port).sync().channel();
            SendService sendService = new SendService(channel);
            sendService.sendMsg();

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if(group != null){
                group.shutdownGracefully();
            }
        }
    }
}
