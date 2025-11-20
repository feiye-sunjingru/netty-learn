package com.feiye.netty1;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;

import java.net.InetSocketAddress;

public class HelloClient {
    public static void main(String[] args) throws InterruptedException {
        //1. 启动类
        new Bootstrap()
                //2. 添加EventLoop
                .group(new NioEventLoopGroup())
                //3. 选择客户端channel实现
                .channel(NioSocketChannel.class)
                //4. 添加处理器
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    //在连接建立后被调用
                    protected void initChannel(NioSocketChannel channel) throws Exception {
                        //编码器，将要发送的消息转为ByteBuf
                        channel.pipeline().addLast(new StringEncoder());
                    }
                })
                //5. 连接到服务器
                .connect(new InetSocketAddress(8080))
                //阻塞方法，直到连接建立
                .sync()
                //代表连接对象
                .channel()
                //6. 向服务端发送数据
                .writeAndFlush("hello server");
    }
}
