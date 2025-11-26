package com.feiye.advance.linebased;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Server3 {
    public static void main(String[] args) {
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup workers = new NioEventLoopGroup();

        ServerBootstrap serverBootstrap = new ServerBootstrap()
                .group(boss, workers)
                .channel(NioServerSocketChannel.class);
        //无论是否设置不影响这里FixedLengthFrameDecoder的输出
        serverBootstrap.childOption(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(16, 16, 16));
        try {
            ChannelFuture channelFuture = serverBootstrap
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            //LineBasedFrameDecoder: 如果输入数据长度超过20字节还没有换行符，则报错；如果catch住，则能继续正常输出
                            //
                            ByteBuf delimiter1 = Unpooled.copiedBuffer("\n".getBytes());
                            ByteBuf delimiter2 = Unpooled.copiedBuffer("\r\n".getBytes());
                            ch.pipeline().addLast(new DelimiterBasedFrameDecoder(20, delimiter1, delimiter2));
//                            ch.pipeline().addLast(new LineBasedFrameDecoder(20));
                            //获取正确解码后的消息
                            ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                        }
                    }).bind(8088).sync();

            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            boss.shutdownGracefully();
            workers.shutdownGracefully();
        }
    }
}
