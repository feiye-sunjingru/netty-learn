package com.ubug.thread;

import java.util.concurrent.CompletableFuture;

public class CallerThread {
    public static void main(String[] args) {
        System.out.println("CallerThread");
        CompletableFuture.runAsync(() -> {
            System.out.println("thread-1");
        });
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("thread-2");
        });
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("thread-3");
        });
        System.out.println("thread-main");
    }
}
