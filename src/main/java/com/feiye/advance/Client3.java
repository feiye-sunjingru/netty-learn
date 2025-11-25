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

import java.util.Random;

@Slf4j
public class Client3 {
    public static void main(String[] args) {
        send();
    }

    /**
     * 填充字符串
     *
     * @param ch  填充的字符
     * @param len 填充的长度
     * @return 填充的字符串
     */
    public static StringBuilder makeString(char ch, int len) {
        StringBuilder sb = new StringBuilder(len);
        sb.append(String.valueOf(ch).repeat(Math.max(0, len)));
        sb.append('\n');
        return sb;
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
//                                    for (int i = 0; i < 10; i++) {
//                                        StringBuilder sb = makeString(c++, random.nextInt(256) + 1);
//                                        buf.writeBytes(sb.toString().getBytes());
//                                    }
//                                    buf.writeBytes("hello\r wo\r\nrld\n".getBytes());
                                    buf.writeBytes("01234556789abcdefghijklmnopqrst\n".getBytes());
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
