package com.feiye.netty1;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * future：空书包，另一个线程装东西
 */
@Slf4j
public class TestJdkFuture {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 1、线程池
        ExecutorService poolExecutor = Executors.newFixedThreadPool(2);
        /*ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(
                2,
                2,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy()
        );*/
        // 2、提交任务:callable有返回值；runnable没有返回值
        Future<Integer> future = poolExecutor.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                log.debug("执行计算...");
                TimeUnit.SECONDS.sleep(1);
                return 50;
            }
        });
        // 3、主线程通过 Future 来获取结果
        log.debug("等待结果...");
        log.debug("结果是 {}", future.get());
    }
}
