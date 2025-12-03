package com.feiye.source;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * 从ChannelConfig找配置-DefaultChannelConfig-ByteBufAllocator-ByteBufUtil
 * UnpooledByteBufAllocator
 *
 * AbstractNioByteChannel-RecvByteBufAllocator.allocate(allocator):
 * RecvByteBufAllocator 是bytebuf的分配器（池化/非池化）
 * allocate方法决定大小、直接/堆内存
 * guess根据之前接收的数据量调整大小
 */
@Slf4j
public class TestByteBuf {
    public static void main(String[] args) {
        System.setProperty("io.netty.allocator.type", "unpooled");
        System.setProperty("io.netty.noPreferDirect", "true");

        new ServerBootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
//                .childOption(ChannelOption.ALLOCATOR, new UnpooledByteBufAllocator(true))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new LoggingHandler());
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                ByteBuf buf = ctx.alloc().buffer();
                                //PooledUnsafeDirectByteBuf(ridx: 0, widx: 0, cap: 256)
                                log.debug("alloc buf: {}", buf);

                                //InstrumentedUnpooledUnsafeDirectByteBuf(ridx: 0, widx: 6, cap: 1024)
                                /*log.debug("receive buf: {}", msg);
                                System.out.println("");*/
                            }
                        });
                    }
                }).bind(8080);
    }
}
