package com.feiye.netty1;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;

/**
 * @desc
 * @auth llp
 * @date 2022/8/8 15:25
 */
@Slf4j
public class EchoClient {
    public static void main(String[] args) throws InterruptedException {
        ChannelFuture channelFuture = new Bootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new ChannelInboundHandlerAdapter() {
                                    @Override   // 建立连接后触发
                                    public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                        ByteBuf buf = ctx.alloc().buffer(10);
                                        //AdaptivePoolingAllocator$AdaptiveByteBuf
                                        log.debug("channelActive: {}, class:{}", buf, buf.getClass());
                                        // 首次连接发送 hello
                                        buf.writeBytes("hello I am Client.".getBytes());
                                        ctx.writeAndFlush(buf);
                                        // 思考：是否需要释放 buf: 需要
                                        buf.release();
                                    }

                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        // 思考：是否需要 buf 释放？:需要AdaptivePoolingAllocator$AdaptiveByteBuf
                                        ByteBuf buf = msg instanceof ByteBuf ? (ByteBuf) msg : null;
                                        if (buf != null) {
                                            log.debug("channelRead:{}, class:{}", buf, buf.getClass());
                                            System.out.println(buf.toString(Charset.defaultCharset()));
                                            buf.release();
                                        } else {
                                            System.out.println("null");
                                        }
                                    }
                                });
                    }
                })
                .connect("localhost", 8888);
    }
}
