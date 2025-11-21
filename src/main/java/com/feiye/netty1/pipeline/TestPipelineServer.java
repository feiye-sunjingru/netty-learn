package com.feiye.netty1.pipeline;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;

/**
 * 入站处理器：只有从channel读取数据才会触发
 * 出站处理器：只有往channel写入才会触发
 */
@Slf4j
public class TestPipelineServer {
    public static void main(String[] args) {
        new ServerBootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        // 1、通过 Channel 获取 pipeline
                        ChannelPipeline pipeline = ch.pipeline();
                        // 2、添加处理器 head -> 添加的handler -> tail（head\tail是netty自动加的，addlast是放到tail之前）
                        // 添加顺序：head -> h1 -> h2 -> h3 -> h4 -> h5 -> h6 -> tail
                        // 入站处理 head -> h1 -> h2 -> h3
                        pipeline.addLast("h1", new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                log.debug("1");
                                // 第一个 handler 将 ByteBuf 转成 String
                                ByteBuf buf = (ByteBuf) msg;
                                String name = buf.toString(Charset.defaultCharset());
                                // 将当前 ChannelInboundHandlerAdapter 处理后的消息传递给下一个入站处理器（inbound handler）
                                // 如果不调用这个方法（或者类似的操作，如 ctx.fireChannelRead(msg)），那么处理链就会中断，后续的入站处理器将不会接收到这个消息
                                super.channelRead(ctx, name);
                            }
                        });
                        pipeline.addLast("h2", new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object name) throws Exception {
                                log.debug("2");
                                // 第二个 handler 将 String 转成 Stu 对象
                                Student student = new Student(name.toString());
//                                // 将数据传递给下一个 handler
                                super.channelRead(ctx, student);
                            }
                        });

                        // 出站处理 (只有向客户端写数据才会触发)  tail -> h6 -> h5 -> h4
                        pipeline.addLast("h4", new ChannelOutboundHandlerAdapter() {
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                log.debug("4");
                                super.write(ctx, msg, promise);
                            }
                        });

                        pipeline.addLast("h3", new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                log.debug("3, 结果:{}, class:{}", msg, msg.getClass());

                                // 向客户端写数据
                                ByteBuf buffer = ctx.alloc().buffer();
                                buffer.writeBytes("server...".getBytes());
                                //从tail往前找出站处理器, 直至head
                                ch.writeAndFlush(buffer);
                                //注意：这是按添加顺序往前找出站处理器进行处理,直至head
//                                ctx.writeAndFlush(buffer);
                            }
                        });

                        pipeline.addLast("h5", new ChannelOutboundHandlerAdapter() {
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                log.debug("5");
                                super.write(ctx, msg, promise);
                            }
                        });
                        pipeline.addLast("h6", new ChannelOutboundHandlerAdapter() {
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                log.debug("6");
                                super.write(ctx, msg, promise);
                            }
                        });
                    }
                })
                .bind(8080);
    }

    @Data
    @AllArgsConstructor
    static class Student{
        private String name;
    }
}
