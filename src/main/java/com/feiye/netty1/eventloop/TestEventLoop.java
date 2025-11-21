package com.feiye.netty1.eventloop;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.NettyRuntime;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * 因为有线程池存在，所以它不会自动停止
 */
@Slf4j
public class TestEventLoop {
    public static void main(String[] args) {
        // 1、创建事件循环组：
        // 处理IO事件、普通任务、定时任务: DEFAULT_EVENT_LOOP_THREADS = Math.max(1, SystemPropertyUtil.getInt("io.netty.eventLoopThreads", NettyRuntime.availableProcessors() * 2));}
        EventLoopGroup group = new NioEventLoopGroup(2);
        System.out.println(NettyRuntime.availableProcessors());
        //处理普通任务、定时任务
        //EventLoopGroup group = new DefaultEventLoop();

        // 2、获取下一个事件循环对象
        // io.netty.channel.nio.NioEventLoop@28261e8e
        System.out.println(group.next());
        // io.netty.channel.nio.NioEventLoop@d737b89
        System.out.println(group.next());
        // io.netty.channel.nio.NioEventLoop@28261e8e (循环回第一个)
        System.out.println(group.next());

        // 3、执行普通任务: 提交给某个eventloop执行
        //做异步处理：当前线程不想完成，交给其他线程处理
         /*group.next().submit(()->{
             try {
                 TimeUnit.SECONDS.sleep(1);
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }
             log.debug("ok");
         });
         log.debug("main");*/

        /*group.next().execute(()->{
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("[" + Thread.currentThread().getName() + "] ok");
        });
        System.out.println("[" + Thread.currentThread().getName() + "] main");*/

        // 4、执行定时任务
        // 固定频率执行任务 参数1: 任务对象，参数2：初始延时事件，参数3：间隔时间，参数4：时间单位
        group.next().scheduleAtFixedRate(() -> {
            System.out.println("[" + Thread.currentThread().getName() + "] scheduleAtFixedRate");
        }, 0, 1, TimeUnit.SECONDS);
    }
}
