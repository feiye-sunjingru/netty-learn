package com.feiye.advance.chatroom.client;

import com.feiye.advance.chatroom.message.RpcRequestMessage;
import com.feiye.advance.chatroom.protocol.MessageCodecSharable;
import com.feiye.advance.chatroom.protocol.ProcotolFrameDecoder;
import com.feiye.advance.chatroom.protocol.SequenceIdGenerator;
import com.feiye.advance.chatroom.server.handler.RpcResponseMessageHandler;
import com.feiye.advance.chatroom.server.service.HelloService;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static com.feiye.advance.chatroom.server.handler.RpcResponseMessageHandler.PROMISES;

/**
 * 把发送消息简化：不必写死在代码里，自动处理类型信息
 */
@Slf4j
public class RpcClientManager {
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

        HelloService proxyService = getProxyService(HelloService.class);
        proxyService.sayHello("张三");
        proxyService.sayHello("李四");
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
        Object proxyInstance = Proxy.newProxyInstance(classLoader, interfaces, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                //1.将方法调用转换为消息对象
                int sid = SequenceIdGenerator.nextId();
                RpcRequestMessage msg = new RpcRequestMessage(
                        sid,
                        serviceClass.getName(),
                        method.getName(),
                        method.getReturnType(),
                        method.getParameterTypes(),
                        args);
                //2.发送消息对象
                getChannel().writeAndFlush(msg);
                // 接受信息是nioEventLoop在接受，这里线程要想获取结果通过promise.
                //第二个参数是执行异步处理处理的线程。这里是同步的方式主线程await等待。
                /*DefaultPromise<Object> promise = new DefaultPromise<>(getChannel().eventLoop());
                // 将「空书包promise」放进map中，供另一个线程放结果。
                PROMISES.put(sid, promise);
                // 等待返回结果
                promise.await();
                if (promise.isSuccess()) {
                    return promise.getNow();
                } else {
                    throw new RuntimeException(promise.cause());
                }*/
                return null;
            }
        });
        return (T) proxyInstance;
    }

    /**
     * 单例获取channel
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
                ch.pipeline().addLast(new ProcotolFrameDecoder());
                ch.pipeline().addLast(LOGGING_HANDLER);
                ch.pipeline().addLast(MESSAGE_CODEC);
                ch.pipeline().addLast(RPC_HANDLER);
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