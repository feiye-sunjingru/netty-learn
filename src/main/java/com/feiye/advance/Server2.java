package com.feiye.advance;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Server2 {
    public static void main(String[] args) {
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup workers = new NioEventLoopGroup();

        ServerBootstrap serverBootstrap = new ServerBootstrap()
                .group(boss, workers)
                .channel(NioServerSocketChannel.class);
        //无论是否设置不影响这里FixedLengthFrameDecoder的输出
        serverBootstrap.childOption(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(16, 16, 16));
        try {
            ChannelFuture channelFuture = serverBootstrap
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new FixedLengthFrameDecoder(10));
                            //获取正确解码后的消息
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
