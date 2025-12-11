package com.feiye.advance.chatroom.client;

import com.feiye.advance.chatroom.message.RpcRequestMessage;
import com.feiye.advance.chatroom.protocol.MessageCodecSharable;
import com.feiye.advance.chatroom.protocol.ProcotolFrameDecoder;
import com.feiye.advance.chatroom.protocol.SequenceIdGenerator;
import com.feiye.advance.chatroom.server.handler.RpcResponseMessageHandler;
import com.feiye.advance.chatroom.server.service.HelloService;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;

import static com.feiye.advance.chatroom.server.handler.RpcResponseMessageHandler.PROMISES;

/**
 * 把发送消息简化：不必写死在代码里，自动处理类型信息
 * <Object> 表示“我知道它是 Object”，<?> 表示“我不知道它是什么类型”(只能get不能set,可以放null)。
 * <p>
 * 注意：
 * 如果你在调试过程中查看了 proxyService 对象的内容（比如悬停鼠标或者加入 Watches），IDE 为了展示对象信息通常会调用 toString() 方法。
 * 这会导致通过代理拦截并发送一个 RpcRequestMessage 到服务端去执行 toString() 方法。
 */
@Slf4j
public class RpcClientManager {
    //不用加volatile: 没有指令重排及半初始化问题
    private static Channel channel;
    private static final Object LOCK = new Object();

    public static void main(String[] args) {
        /*getChannel().writeAndFlush(new RpcRequestMessage(
                1,
                "com.feiye.advance.chatroom.server.service.HelloService",
                "sayHello",
                String.class,
                new Class[]{String.class},
                new Object[]{"张三"}
        ));*/

        //通过代理对象拿到 HelloService
        HelloService proxyService = getProxyService(HelloService.class);
        //主线程发起调用
        System.out.println(proxyService.sayHello("张三"));
        /*System.out.println(proxyService.sayHello("李四"));
        System.out.println(proxyService.sayHello("王五"));*/
    }

    /**
     * 使用代理屏蔽复杂过程：代理任一service，直接执行方法即可
     *
     * @param serviceClass
     * @param <T>
     * @return 获取代理对象
     */
    public static <T> T getProxyService(Class<T> serviceClass) {
        ClassLoader classLoader = serviceClass.getClassLoader();
        Class<?>[] interfaces = new Class[]{serviceClass};

        // 创建代理对象: 类加载器，代理类实现的接口，代理要做的事情
        // 1. 创建动态代理
        // 2. 拦截所有方法调用
        // 3. 自动发起远程调用
        Object proxyInstance = Proxy.newProxyInstance(classLoader, interfaces, (proxy, method, args) -> {
            System.out.println("触发方法: " + method.getName());
            //1.将方法调用转换为消息对象
            int sid = SequenceIdGenerator.nextId();
            //sid是消息的key
            RpcRequestMessage msg = new RpcRequestMessage(
                    sid,
                    serviceClass.getName(),
                    method.getName(),
                    method.getReturnType(),
                    method.getParameterTypes(),
                    args);
            //2.发送消息对象
            Channel ch = getChannel();
            ch.writeAndFlush(msg).addListener(new ChannelFutureListener() {

                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (!future.isSuccess()) {
                        log.error("出站异常：", future.cause());
                        ch.close();
                    }
                }
            });
            // 多个线程之间接收结果: 准备一个空书包
            //异步接收结果，使用其他线程eventloop
            DefaultPromise<Object> promise = new DefaultPromise<>(getChannel().eventLoop());
            // 将「空书包promise」放进map中，供另一个线程放结果。
            PROMISES.put(sid, promise);
            // 等待返回结果:sync失败会抛异常，await失败不会抛异常，通过isSuccess继续判断。
            promise.await();

            //如果出错可以返回给主线程
            if (promise.isSuccess()) {
                //需要等有结果了再返回给用户
                return promise.getNow();
            } else {
                throw new RuntimeException(promise.cause());
            }
        });
        return (T) proxyInstance;
    }

    /**
     * 单例获取channel：主要是客户端存在多个线程
     * 双检锁
     *
     * @return channel
     */
    private static Channel getChannel() {
        if (channel != null) {
            return channel;
        }
        synchronized (LOCK) {
            //抢到锁：如果此时不为空，则直接返回。
            if (channel != null) {
                return channel;
            }
            initChannel();
            return channel;
        }
    }

    /**
     * 初始化 channel
     */
    private static void initChannel() {
        NioEventLoopGroup group = new NioEventLoopGroup();
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        MessageCodecSharable MESSAGE_CODEC = new MessageCodecSharable();
        // rpc 响应消息处理器，待实现
        RpcResponseMessageHandler RPC_HANDLER = new RpcResponseMessageHandler();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.group(group);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast(new ProcotolFrameDecoder());
                pipeline.addLast(LOGGING_HANDLER);
                pipeline.addLast(MESSAGE_CODEC);
                pipeline.addLast(RPC_HANDLER);
                pipeline.addLast("client handler", new ChannelInboundHandlerAdapter() {
                    @Override  //连接已失效：此时 不能再通过该 Channel 发送数据
                    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                        // 继续传播事件（通常建议调用）
                        ctx.fireChannelInactive();
                        //System.out.println(1/0);
                        // 这样会直接输出“异常”，没有连接已失效
                        log.debug("连接已失效");
                    }

                    @Override
                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                        log.debug("入站处理异常");
                        ctx.close();
                    }
                });
            }
        });
        try {
            //连接建立好了才可用：必须sync
            channel = bootstrap.connect("localhost", 8080).sync().channel();
            //sync:阻塞当前线程、等待 Channel 关闭完成; 如果sync就会一直等待channel关闭，永远到达不了getChannel
            channel.closeFuture().addListener(future -> {
                group.shutdownGracefully();
            });
        } catch (Exception e) {
            log.error("client error", e);
        }
    }
}