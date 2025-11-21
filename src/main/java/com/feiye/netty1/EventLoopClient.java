package com.feiye.netty1;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * 把数据发出去的线程并不是主线程
 * idea suspend all: 停下来所有线程；切换成Thread主线程
 */
@Slf4j
public class EventLoopClient {
    public static void main(String[] args) throws InterruptedException {
        // 1、客户端启动器
        // 带有Future、Promise的类型都是和异步方法配套使用：用来处理结果
        ChannelFuture channelFuture = new Bootstrap()
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
                // 5、连接到服务器:
                // 异步非阻塞：main发起调用，然后会继续执行，不会等待连接真正建立；真正去connect的是Nio中的线程
                .connect(new InetSocketAddress("localhost", 8080));

        //2.1 使用sync同步处理结果
        //会阻塞，等待连接真正建立
        /*channelFuture.sync();
        //无阻塞向下获取Channel：channel:[id: 0x6fd58614] VS [id: 0x19495cc0, L:/127.0.0.1:54205 - R:localhost/127.0.0.1:8080]
        Channel channel = channelFuture.channel();
        log.debug("channel:{}", channel);
        channel.writeAndFlush("hello world");*/

        //2.2 使用addListener(回调对象)异步处理结果
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                Channel channel = future.channel();
                log.debug("channel:{}", channel);
                channel.writeAndFlush("hello world");
            }
        });

    }
}
