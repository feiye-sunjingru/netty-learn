package com.feiye.advance;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Random;

@Slf4j
public class Client2 {
    public static void main(String[] args) {
        /*fill10Bytes('1', 5);
        fill10Bytes('2', 2);
        fill10Bytes('3', 10);
        System.out.println("finish");*/

        send();
    }

    public static byte[] fill10Bytes(char ch, int len) {
        byte[] bytes = new byte[10];
        Arrays.fill(bytes, (byte) '_');
        for (int i = 0; i < len; i++) {
            bytes[i] = (byte) ch;
        }
        System.out.println(new String(bytes));
        return bytes;
    }

    private static void send() {
        NioEventLoopGroup workers = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        try {
            ChannelFuture channelFuture = bootstrap
                    .channel(NioSocketChannel.class)
                    .group(workers)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                @Override //channel连接成功后会触发active事件；也可以在connect.sync之后获取channel处理
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    char c = '0';
                                    Random random = new Random();
                                    //一次发送一个buf
                                    ByteBuf buf = ctx.alloc().buffer();
                                    for (int i = 0; i < 10; i++) {
                                        byte[] bytes = fill10Bytes(c++, random.nextInt(10) + 1);
                                        buf.writeBytes(bytes);
                                    }
                                    ctx.writeAndFlush(buf);
                                }
                            });
                        }
                    }).connect("localhost", 8088).sync();

            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("client error", e);
        } finally {
            workers.shutdownGracefully();
        }
    }
}
