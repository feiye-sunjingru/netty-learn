package com.feiye.netty1;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;

import java.net.InetSocketAddress;
import java.util.Scanner;

/**
 * 把数据发出去的线程并不是主线程
 * idea suspend all: 停下来所有线程；切换成Thread主线程
 */
public class EventLoopClient {
    public static void main(String[] args) throws InterruptedException {
        // 1、客户端启动器
        Channel channel = (Channel) new Bootstrap()
                // 2、添加 EventLoop
                .group(new NioEventLoopGroup())
                // 3、选择客户端 Channel 事件
                .channel(NioSocketChannel.class)
                // 4、添加处理器,
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override   // 连接建立后被调用
                    protected void initChannel(NioSocketChannel ch) {
                        ch.pipeline().addLast(new StringEncoder());
                    }
                })
                // 5、连接到服务器
                .connect(new InetSocketAddress("localhost", 8080))
                .sync()
                .channel();
        System.out.println(channel);
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String msg = scanner.nextLine();
            channel.writeAndFlush(msg);
        }
    }
}
