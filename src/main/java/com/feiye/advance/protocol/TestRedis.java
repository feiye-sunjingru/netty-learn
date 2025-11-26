package com.feiye.advance.protocol;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;

/**
 * sudo nano /etc/redis/redis.conf
 *
 * # 允许外部 IP 连接（不要用 0.0.0.0 生产环境！开发可临时用）
 * bind 192.168.12.5
 * # 关闭保护模式（否则只允许本地连接）
 * protected-mode no
 * # 设置密码（强烈建议！）
 * requirepass your_strong_password
 * # 确保端口是 6379（默认）
 * port 6379
 *
 * sudo systemctl restart redis-server
 *
 * 这里按照redis的协议发送数据给redis,并接收redis的结果
 */
@Slf4j
public class TestRedis {
    public static void main(String[] args) {
        final byte[] LINE = {13, 10};   // \r \n
        NioEventLoopGroup worker = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.group(worker);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new LoggingHandler());
                    ch.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                        @Override   // set name zhangsan 按协议发送给redis
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            ByteBuf byteBuf = ctx.alloc().buffer();
                            byteBuf.writeBytes("*3".getBytes());
                            byteBuf.writeBytes(LINE);

                            byteBuf.writeBytes("$3".getBytes());
                            byteBuf.writeBytes(LINE);
                            byteBuf.writeBytes("set".getBytes());
                            byteBuf.writeBytes(LINE);

                            byteBuf.writeBytes("$3".getBytes());
                            byteBuf.writeBytes(LINE);
                            byteBuf.writeBytes("age".getBytes());
                            byteBuf.writeBytes(LINE);
                            //17是两个字节
                            byteBuf.writeBytes("$2".getBytes());
                            byteBuf.writeBytes(LINE);
                            byteBuf.writeBytes("17".getBytes());
                            byteBuf.writeBytes(LINE);

                            ctx.writeAndFlush(byteBuf);
                        }
                        @Override //接收redis结果
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            ByteBuf buf = (ByteBuf) msg;
                            System.out.println(buf.toString(Charset.defaultCharset()));
                        }
                    });
                }
            });
            ChannelFuture channelFuture = bootstrap.connect("192.168.12.5", 6379).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("client error", e);
        } finally {
            worker.shutdownGracefully();
        }
    }
}
