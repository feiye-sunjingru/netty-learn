package com.feiye.advance.protocol;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * 浏览器输入 http://localhost:8888/ 处理浏览器返回结果
 */
@Slf4j
public class TestHttp {
    public static void main(String[] args) {
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.group(boss, worker);
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                    // HttpServerCodec 编解码器（入站+出站处理器），解码器解析成两部分: 请求行和请求头；请求体
                    ch.pipeline().addLast(new HttpServerCodec());
                    // 第一种
                    /*ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            // class i.n.h.codec.http.DefaultHttpRequest 请求行和请求头和 i.n.h.codec.http.LastHttpContent$1请求体
                            log.debug("msg.getClass:{}", msg.getClass());
                            // 请求行和请求头
                            if (msg instanceof HttpRequest) {

                            } else if (msg instanceof HttpContent) { // 请求体

                            }
                        }
                    });*/
                    // 第二种 也是入站处理器：但是只关心特定消息类型的，如这里关注HttpRequest
                    // ch.pipeline().addLast(new SimpleChannelInboundHandler<HttpContent>() {});
                    ch.pipeline().addLast(new SimpleChannelInboundHandler<HttpRequest>() {
                        @Override
                        protected void channelRead0(ChannelHandlerContext ctx, HttpRequest msg) throws Exception {
                            // 获取请求，headers请求头（一般不关心）
                            log.debug(msg.uri());
                            // http协议版本、响应状态码
                            DefaultFullHttpResponse response =
                                    new DefaultFullHttpResponse(msg.protocolVersion(), HttpResponseStatus.OK);
                            byte[] bytes = "<h1>Hello, World!</h1>".getBytes();
                            // 响应长度，不设置浏览器会一直转圈读取
                            response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, bytes.length);
                            response.content().writeBytes(bytes);
                            // 写回响应，返回给浏览器
                            ctx.writeAndFlush(response);
                        }
                    });

                }
            });
            ChannelFuture channelFuture = serverBootstrap.bind(8888).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("server error", e);
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}
