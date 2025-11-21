package com.feiye.netty1.futuprom;

import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class TestNettyPromise {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 1、准备 EventLoop 对象
        EventLoop eventLoop = new NioEventLoopGroup().next();
        // 2、可以主动创建 Promise：结果容器
        DefaultPromise<Integer> promise = new DefaultPromise<>(eventLoop);

        new Thread(() -> {
            // 3、任意一个线程执行计算，计算完成之后向 promise 填充结果
            log.debug("开始计算...");
            try {
                TimeUnit.SECONDS.sleep(1);
                // 计算出现异常
                int i = 1 / 0;
                //自己装结果
                promise.setSuccess(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
                //线程出错会通知到main线程
                promise.setFailure(e);
            }
        }).start();

        // 4、接收结果的线程
        log.debug("等待结果...");
        log.debug("结果是：{}", promise.get());
        // 异步
//         promise.addListener(future -> log.debug("结果是：{}", future.get()));
    }
}

