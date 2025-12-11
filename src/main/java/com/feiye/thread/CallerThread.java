package com.feiye.thread;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * CompletableFuture.runAsync() 是 Java 8 引入的 异步编程工具类
 * CompletableFuture 中的一个静态方法，用于以异步方式执行一个不返回结果（即 void）的任务。
 */
public class CallerThread {
    public static void main(String[] args) {
        System.out.println("CallerThread");
        // 这里是非阻塞的
        CompletableFuture.runAsync(() -> {
            System.out.println("thread-1");
        });

        //sleep阻塞：当前线程自己暂停一段时间
        CompletableFuture.runAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("thread-2");
        });

        //join阻塞：线程同步：确保某线程执行完毕后再继续
        CompletableFuture.runAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("thread-3");
        }).join();
        System.out.println("thread-main");
    }
}
