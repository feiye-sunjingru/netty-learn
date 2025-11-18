package com.feiye.nio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

import static com.feiye.nio.channelBuffer.ByteBufferUtil.debugAll;

/**
 * boss 线程负责selector连接
 * worker线程负责selector读写
 */
@Slf4j
public class MultiThreadServer {
    public static void main(String[] args) throws IOException {
        Thread.currentThread().setName("boss");
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.bind(new InetSocketAddress(8080));
        ssc.configureBlocking(false);

        Selector boss = Selector.open();
        ssc.register(boss, SelectionKey.OP_ACCEPT);
        //1.创建固定数量的worker: CPU核数
        Worker[] workers = new Worker[Runtime.getRuntime().availableProcessors()];
        for (int i = 0; i < workers.length; i++) {
            workers[i] = new Worker("worker-" + i);
        }

        AtomicInteger index = new AtomicInteger();
        while (true) {
            boss.select();
            Iterator<SelectionKey> iterator = boss.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                if (key.isAcceptable()) {
                    SocketChannel sc = ssc.accept();
                    sc.configureBlocking(false);
                    log.debug("connected...{}", sc.getRemoteAddress());
                    //2.关联selector: 静态内部
                    log.debug("before register...{}", sc.getRemoteAddress());
                    workers[index.getAndIncrement() % workers.length].register(sc);
                    log.debug("after register...{}", sc.getRemoteAddress());
                }
            }
        }
    }

    static class Worker implements Runnable {
        private Thread thread;
        private Selector selector;
        private String name;
        private volatile boolean init = false;
        //两个线程之间传递数据，作为数据通道
        private ConcurrentLinkedDeque<Runnable> queue = new ConcurrentLinkedDeque();

        public Worker(String name) {
            this.name = name;
        }

        public void register(SocketChannel sc) throws IOException {
            if (!init) {
                // 创建selector: 要在线程启动之前
                selector = Selector.open();
                //run才是在子线程里面运行的
                thread = new Thread(this, name);
                thread.start();
                init = true;
            }

            // 向队列添加任务，但是没有立即执行
            queue.add(() -> {
                try {
                    //注册读事件，要在读之前注册好
                    sc.register(selector, SelectionKey.OP_READ, null);
                } catch (ClosedChannelException e) {
                    throw new RuntimeException(e);
                }
            });
            // 唤醒selector：一次性的，可以直接wakeup+sc.register
            selector.wakeup();
        }

        @Override
        public void run() {
            while (true) {
                try {
                    //阻塞
                    selector.select();
                    Runnable task = queue.poll();
                    if (task != null) {
                        task.run(); //执行了sc.register(selector, SelectionKey.OP_READ, null);
                    }
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        iterator.remove();
                        if (key.isReadable()) {
                            SocketChannel sc = (SocketChannel) key.channel();
                            log.debug("readed...{}", sc.getRemoteAddress());
                            ByteBuffer buffer = ByteBuffer.allocate(16);
                            sc.read(buffer);
                            buffer.flip();
                            debugAll(buffer);
                        } else if (key.isWritable()) {
                            SocketChannel sc = (SocketChannel) key.channel();
                            sc.write(ByteBuffer.wrap("hello".getBytes()));
                        }

                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
