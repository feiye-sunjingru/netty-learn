package com.feiye.thread;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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
