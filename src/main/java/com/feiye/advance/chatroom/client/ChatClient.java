package com.feiye.advance.chatroom.client;

import com.feiye.advance.chatroom.message.*;
import com.feiye.advance.chatroom.protocol.MessageCodecSharable;
import com.feiye.advance.chatroom.protocol.ProcotolFrameDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class ChatClient {
    public static void main(String[] args) {
        NioEventLoopGroup group = new NioEventLoopGroup();
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        MessageCodecSharable MESSAGE_CODEC = new MessageCodecSharable();

        //初始计数为1，为了让System in线程能够从阻塞继续执行下去
        CountDownLatch WAIT_FOR_LOGIN = new CountDownLatch(1);

        // 存放登录结果：因为存在并发多个线程会，因此用AtomicBoolean保证线程安全
        AtomicBoolean LOGIN = new AtomicBoolean();

        AtomicBoolean EXIT = new AtomicBoolean();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.group(group);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new ProcotolFrameDecoder());
//                    ch.pipeline().addLast(LOGGING_HANDLER);
                    ch.pipeline().addLast(MESSAGE_CODEC);
                    // 判断是否有读空闲或者写空闲时间过长，会触发一个 IdleState#WRITER_IDLE 事件
                    /*ch.pipeline().addLast(new IdleStateHandler(0, 3, 0));
                    // 对IDLE事件进行处理
                    ch.pipeline().addLast(new ChannelDuplexHandler() {
                        // 用来触发特殊事件
                        @Override
                        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                            IdleStateEvent event = (IdleStateEvent) evt;
                            // 触发了读空闲事件
                            if (event.state() == IdleState.WRITER_IDLE) {
//                                log.debug("已经 3s 没有写数据了,发送一个心跳包");
                                ctx.writeAndFlush(new PingMessage());
                            }
                        }
                    });*/

                    ch.pipeline().addLast("client handler", new ChannelInboundHandlerAdapter() {
                        // 连接建立后执行active事件
                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            // 1 新建一个线程"systemIn"，接受用户输入，并向服务器发送消息: 避免阻塞当前线程; 这个线程只会创建一次
                            new Thread(() -> {
                                Scanner scanner = new Scanner(System.in);
                                System.out.println("请输入用户名:");
                                String username = scanner.nextLine();

                                System.out.println("请输入密码:");
                                String password = scanner.nextLine();

                                LoginRequestMessage message = new LoginRequestMessage(username, password);
                                ctx.writeAndFlush(message);
                                System.out.println("等待后续操作...");

                                // 获取服务端返回的结果才能继续执行。通过CountDownLatch来通信
                                try {
                                    // 2 阻塞直到计数减为0
                                    WAIT_FOR_LOGIN.await();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                if (!LOGIN.get()) {
                                    //3登录失败，关闭channel
                                    ctx.channel().close();
                                    return;
                                }

                                // 3登录成功，显示选择面板
                                while (true) {
                                    System.out.println("==================================");
                                    System.out.println("send [username] [content]");
                                    System.out.println("gsend [group name] [content]");
                                    System.out.println("gcreate [group name] [m1,m2,m3...]");
                                    System.out.println("gmembers [group name]");
                                    System.out.println("gjoin [group name]");
                                    System.out.println("gquit [group name]");
                                    System.out.println("quit");
                                    System.out.println("==================================");
                                    String command = scanner.nextLine();
                                    if (EXIT.get()) {
                                        return;
                                    }
                                    String[] s = command.split(" ");
                                    switch (s[0]) {
                                        // 单聊
                                        case "send":
                                            ctx.writeAndFlush(new ChatRequestMessage(username, s[1], s[2]));
                                            break;
                                        // 群发
                                        case "gsend":
                                            ctx.writeAndFlush(new GroupChatRequestMessage(username, s[1], s[2]));
                                            break;
                                        // 群创建
                                        case "gcreate":
                                            Set<String> set = new HashSet<>(Arrays.asList(s[2].split(",")));
                                            set.add(username); // 加入自己
                                            ctx.writeAndFlush(new GroupCreateRequestMessage(s[1], set));
                                            break;
                                        // 查看群成员
                                        case "gmembers":
                                            ctx.writeAndFlush(new GroupMembersRequestMessage(s[1]));
                                            break;
                                        // 加入群
                                        case "gjoin":
                                            ctx.writeAndFlush(new GroupJoinRequestMessage(username, s[1]));
                                            break;
                                        // 退出群
                                        case "gquit":
                                            ctx.writeAndFlush(new GroupQuitRequestMessage(username, s[1]));
                                            break;
                                        // 退出
                                        case "quit":
                                            ctx.channel().close();
                                            return;
                                    }
                                }
                            }, "systemIn").start();
                        }

                        // 读取服务端返回的信息
                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            log.debug("msg: {}", msg);
                            if (msg instanceof LoginResponseMessage) {
                                LoginResponseMessage loginResp = (LoginResponseMessage) msg;
                                if (loginResp.isSuccess()) {
                                    LOGIN.set(true);
                                }
                                // 让计数减一：读取到登录结果后唤醒system in 线程
                                WAIT_FOR_LOGIN.countDown();
                            }
                        }

                        // 连接断开处理
                        @Override
                        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                            log.debug("连接已经断开，按任意键退出..");
                            EXIT.set(true);
                        }

                        // 出现异常处理
                        @Override
                        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                            log.debug("由于异常连接已经断开，按任意键退出..{}", cause);
                            EXIT.set(true);
                        }
                    });
                }
            });
            Channel channel = bootstrap.connect("localhost", 8080).sync().channel();
            channel.closeFuture().sync();
        } catch (Exception e) {
            log.error("client error", e);
        } finally {
            group.shutdownGracefully();
        }
    }

/*    public void receiveInput() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入用户名:");
        String username = scanner.nextLine();
        if (EXIT.get()) {
            return;
        }
        System.out.println("请输入密码:");
        String password = scanner.nextLine();
        if (EXIT.get()) {
            return;
        }
        LoginRequestMessage message = new LoginRequestMessage(username, password);
        ctx.writeAndFlush(message);
        System.out.println("等待后续操作...");

        // 如果登录成功进入选择界面。如果登录失败，关闭channel
        // 获取服务端返回的结果才能继续执行。通过CountDownLatch来通信
        try {
            WAIT_FOR_LOGIN.await(); // 阻塞，当为0后会继续执行
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (!LOGIN.get()) {
            //登录失败，关闭channel
            ctx.channel().close();
            return;
        }
        // 登录成功，显示选择面板
        while (true) {
            System.out.println("==================================");
            System.out.println("send [username] [content]");
            System.out.println("gsend [group name] [content]");
            System.out.println("gcreate [group name] [m1,m2,m3...]");
            System.out.println("gmembers [group name]");
            System.out.println("gjoin [group name]");
            System.out.println("gquit [group name]");
            System.out.println("quit");
            System.out.println("==================================");
            String command = scanner.nextLine();
            if (EXIT.get()) {
                return;
            }
            String[] s = command.split(" ");
            switch (s[0]) {
                case "send":
                    ctx.writeAndFlush(new ChatRequestMessage(username, s[1], s[2]));
                    break;
                case "gsend":
                    ctx.writeAndFlush(new GroupChatRequestMessage(username, s[1], s[2]));
                    break;
                case "gcreate":
                    Set<String> set = new HashSet<>(Arrays.asList(s[2].split(",")));
                    set.add(username); // 加入自己
                    ctx.writeAndFlush(new GroupCreateRequestMessage(s[1], set));
                    break;
                case "gmembers":
                    ctx.writeAndFlush(new GroupMembersRequestMessage(s[1]));
                    break;
                case "gjoin":
                    ctx.writeAndFlush(new GroupJoinRequestMessage(username, s[1]));
                    break;
                case "gquit":
                    ctx.writeAndFlush(new GroupQuitRequestMessage(username, s[1]));
                    break;
                case "quit":
                    ctx.channel().close();
                    return;
            }
        }
    }*/
}
