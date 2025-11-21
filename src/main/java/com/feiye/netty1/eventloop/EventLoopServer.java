package com.feiye.netty1.eventloop;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@Slf4j
public class EventLoopServer {
    public static void main(String[] args) {
        //细分2：创建一个单独的非nio EventLoopGroup来处理耗时长的操作，避免这个操作耗时过长，导致影响worker处理Channel的读写
        DefaultEventLoopGroup group = new DefaultEventLoopGroup();

        new ServerBootstrap()
                // 细分1 参数1： boss（只负责 ServerSocketChannel 上 accept 事件） :只会占用 参数1（boss）里面的一个线程
                // 参数2： worker（只负责 SocketChannel 上的读写事件）: worker 初始化两个线程（只有两个worker）
                .group(new NioEventLoopGroup(1), new NioEventLoopGroup(2))
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) {
                        ch.pipeline()
                                .addLast("handler1", new ChannelInboundHandlerAdapter() {
                                    @Override   // msg ByteBuf
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) {
                                        ByteBuf buf = (ByteBuf) msg;
                                        log.debug("[{}]{}", Thread.currentThread().getName(), buf.toString(StandardCharsets.UTF_8));
                                        //不是责任链模式：将信息传递给下一个handler，如果不传这里就断掉了
                                        ctx.fireChannelRead(msg);
                                    }
                                    //使用group
                                }).addLast(group, "handler2", new ChannelInboundHandlerAdapter() {
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) {
                                        ByteBuf buf = (ByteBuf) msg;
                                        log.debug("[{}]{}", Thread.currentThread().getName(), buf.toString(StandardCharsets.UTF_8));
                                    }
                                });
                    }
                })
                .bind(8080);
    }
}
