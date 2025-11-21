package com.feiye.netty1.futuprom;

import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class TestNettyFuture {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //netty的线程：EventLoop是一个现成
        NioEventLoopGroup group = new NioEventLoopGroup();
        EventLoop eventLoop = group.next();

        Future<Integer> future = eventLoop.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                log.debug("执行计算...");
                TimeUnit.SECONDS.sleep(3);
                return 50;
            }
        });
        log.debug("等待结果...");
        // 同步方式[main]
        //log.debug("结果是：{}", future.get());
        // 异步方式
        future.addListener(new GenericFutureListener<Future<? super Integer>>() {
            @Override
            public void operationComplete(Future<? super Integer> future1) throws Exception {
                //此时用get也可以，因为线程到这里已经拿到结果了[nioEventLoopGroup-2-1]
                log.debug("结果是：{}", future1.getNow());
            }
        });
    }
}
