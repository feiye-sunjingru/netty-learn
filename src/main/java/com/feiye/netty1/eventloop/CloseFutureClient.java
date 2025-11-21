package com.feiye.netty1.eventloop;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Scanner;

/**
 * 不断往客户端发送数据，q退出
 */
@Slf4j
public class CloseFutureClient {

    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();
        ChannelFuture channelFuture = new Bootstrap()
                .channel(NioSocketChannel.class)
                .group(group)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                        ch.pipeline().addLast(new StringEncoder());
                    }
                })
                .connect("localhost", 8080);
        Channel channel = channelFuture.sync().channel();
        log.debug("channel:{}", channel);
        //处理用户在控制台的输入
        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String line = scanner.next();
                if (line.equals("q")) {
                    //[nioEventLoopGroup-2-1]异步操作，交给其他线程处理
                    channel.close();
                    // [Thread-0]:debug 与 close 不是同一线程执行放的无法保证顺序:不能在这里添加
                    //log.debug("处理关闭之后的操作");
                    break;
                }
                channel.writeAndFlush(line);
            }
        }).start();

        // * 推荐两种种方式关闭 channel 1同步 2异步
        ChannelFuture closeFuture = channel.closeFuture();
        System.out.println("waiting close...");
        closeFuture.sync();
        log.debug("处理关闭之后的操作");
        group.shutdownGracefully();

        //谁close谁调用该回调方法
        /*closeFuture.addListener(new ChannelFutureListener() {

            @Override
            public void operationComplete(ChannelFuture future) {
                log.debug("处理关闭之后的操作");
                group.shutdownGracefully();
            }
        });*/
    }
}