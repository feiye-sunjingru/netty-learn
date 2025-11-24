package com.feiye.advance;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HelloWorldClient {
    public static void main(String[] args) {
        NioEventLoopGroup workers = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        try {
            ChannelFuture channelFuture = bootstrap
                    .channel(NioSocketChannel.class)
                    .group(workers)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                @Override //channel连接成功后会触发active事件；也可以在connect.sync之后获取channel处理
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    //发送10次的结果被服务器一下子全部接收完了：黏包现象
                                    for (int i = 0; i < 10; i++) {
                                        //记得要放在里面才会循环写入：否则只会写入一次，因为读写指针都到了最后
                                        ByteBuf buf = ctx.alloc().buffer(16);
                                        buf.writeBytes(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15});
                                        ctx.writeAndFlush(buf);
                                    }
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
