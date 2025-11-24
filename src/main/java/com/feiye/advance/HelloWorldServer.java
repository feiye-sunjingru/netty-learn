package com.feiye.advance;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HelloWorldServer {
    public static void main(String[] args) {
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup workers = new NioEventLoopGroup();

        ServerBootstrap serverBootstrap = new ServerBootstrap()
                .group(boss, workers)
                .channel(NioServerSocketChannel.class);
        //调整系统的接收缓冲区（滑动窗口）
//        serverBootstrap.option(ChannelOption.SO_RCVBUF, 10);
        //调整netty的接收缓冲区（byteBuf）:childOption针对是每个channel连接的: 如果小于客户端的bytebuf大小16，服务端无法读入客户端数据
        serverBootstrap.childOption(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(16, 16, 16));
        System.out.println();
        try {
            ChannelFuture channelFuture = serverBootstrap
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            //调试打印服务端接收的消息
                            ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                        }
                    }).bind(8088).sync();

            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("server error", e);
        } finally {
            boss.shutdownGracefully();
            workers.shutdownGracefully();
        }
    }
}
