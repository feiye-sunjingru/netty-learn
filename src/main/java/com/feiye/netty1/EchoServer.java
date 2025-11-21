package com.feiye.netty1;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;

/**
 * @desc
 * @auth llp
 * @date 2022/8/8 15:19
 */
@Slf4j
public class EchoServer {
    public static void main(String[] args) {
        new ServerBootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        // 入站处理器
                        ch.pipeline().addLast("readHandler", new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                // 思考：是否需要 buf 释放？AdaptivePoolingAllocator$AdaptiveByteBuf：需要
                                ByteBuf buf = msg instanceof ByteBuf ? (ByteBuf) msg : null;
                                if (buf != null) {
                                    log.debug("buf:{}, class:{}", buf, buf.getClass());
                                    System.out.println(buf.toString(Charset.defaultCharset()));
                                    //buf.release();
                                    ReferenceCountUtil.release(buf);
                                } else {
                                    System.out.println("null");
                                }
                                // 建议使用 ctx.alloc() 创建 ByteBuf
                                ByteBuf response = ctx.alloc().buffer();
                                log.debug("response:{}, class:{}", response, response.getClass());
                                response.writeBytes("hello I am Server.".getBytes());
                                ctx.writeAndFlush(response);
                                // 思考：response 是否需要释放：AdaptivePoolingAllocator$AdaptiveByteBuf 需要
                                response.release();
                            }
                        });
                    }
                })
                .bind(8888);
    }
}
