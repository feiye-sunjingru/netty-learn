package com.feiye.netty1;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;

public class HelloServer {
    public static void main(String[] args) throws InterruptedException {
        //1. 启动器，负责组装netty组件，启动服务器
        new ServerBootstrap()
                //2. BootEventLoop WorkerEventLoop(selector,thread),group组
                .group(new NioEventLoopGroup())
                //3. 选择服务器的ServerSocketChannel实现
                .channel(NioServerSocketChannel.class)
                //4. boss负责处理连接，worker(child)负责处理读写，决定了worker(child)能执行哪些操作（handler）
                .childHandler(
                        //5. channel代表和客户端进行读写的通道,Initializer初始化器，负责添加别的handler
                        new ChannelInitializer<NioSocketChannel>() {
                            @Override
                            //与客户端建立连接后被调用
                            protected void initChannel(NioSocketChannel channel) throws Exception {
                                //6. 添加具体的handler
                                channel.pipeline()
                                        //将传输过的ByteBuf转换为字符串
                                        .addLast(new StringDecoder())
                                        //添加自己的业务处理，自定义的handler
                                        .addLast(new ChannelInboundHandlerAdapter() {
                                            @Override
                                            //读事件
                                            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                                                //打印转换好后的字符串
                                                System.out.println(msg);
                                            }
                                        });
                            }
                        })
                //7. 绑定监听端口
                .bind(8080).sync();
    }
}
