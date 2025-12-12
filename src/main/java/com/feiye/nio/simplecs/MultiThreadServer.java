package com.feiye.nio.simplecs;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

import static com.feiye.nio.TestSplit.split;
import static com.feiye.nio.channelBuffer.ByteBufferUtil.debugAll;

/**
 * boss 线程负责selector连接
 * worker线程负责selector读写
 * <p>
 * Worker类：Selector
 * 没有sc是因为在 boss线程 处理ccept时已经产生了
 * <p>
 * 粘包半包+客户端中断+写事件分多次
 * <p>
 * 这里注意的是：sc跟worker.selector绑定需要在Worker.register之后执行
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

        // index使用都是在boss线程，可以使用int
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
                    //2.关联selector: 静态内部,worker线程运行
                    workers[index.getAndIncrement() % workers.length].initWorker(sc);
                }
            }
        }
    }

    //implements Runnable： 定义了一个可以被线程执行的任务
    static class Worker implements Runnable {
        private Thread thread;
        private Selector selector;
        private String name;
        // 为了保证可见性:volatile 确保一个线程对 init 的修改对其他线程立即可见
        private volatile boolean init = false;
        //两个线程之间传递数据，作为数据通道
        private ConcurrentLinkedDeque<Runnable> queue = new ConcurrentLinkedDeque();

        public Worker(String name) {
            this.name = name;
        }

        public void initWorker(SocketChannel sc) throws IOException {
            if (!init) {
                // 创建selector: 要在线程启动之前
                selector = Selector.open();
                //run才是在子线程里面运行的：这个worker就是任务对象
                thread = new Thread(this, name);
                thread.start();
                init = true;
            }

            // 向队列添加任务，但是没有立即执行
            queue.add(() -> {
                try {
                    ByteBuffer buffer = ByteBuffer.allocate(16);
                    //注册读事件，要在读之前注册好
                    log.debug("before register...{}", sc.getRemoteAddress());
                    sc.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, buffer);
                    log.debug("after register...{}", sc.getRemoteAddress());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            // 唤醒selector：一次性的，可以直接wakeup+sc.register
            selector.wakeup();
            /*// 也可以这样做：无论select()在哪儿阻塞都能保证注册之后执行阻塞后面的步骤
            selector.wakeup();
            sc.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, buffer);*/
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
                            try {
                                SocketChannel sc = (SocketChannel) key.channel();
                                log.debug("readed...{}", sc.getRemoteAddress());

                                ByteBuffer buffer = (ByteBuffer) key.attachment();
                                int read = sc.read(buffer);
                                if (read == -1) {
                                    key.cancel();
                                } else {
                                    split(buffer);
                                    // 说明buffer满了没有被压缩掉
                                    if (buffer.position() == buffer.limit()) {
                                        ByteBuffer newBuffer = ByteBuffer.allocate(buffer.capacity() * 2);
                                        buffer.flip();
                                        newBuffer.put(buffer);
                                        key.attach(newBuffer);
                                    }
                                    debugAll(buffer);
                                }
                            } catch (IOException e) {
                                key.cancel();
                                e.printStackTrace();
                            }
                        } else if (key.isWritable()) {
                            SelectableChannel channel = key.channel();
                            if (channel instanceof SocketChannel) {
                                ByteBuffer buffer = (ByteBuffer) key.attachment();
                                int write = ((SocketChannel) channel).write(buffer);
                                log.debug("write...{}", write);
                                if (!buffer.hasRemaining()) {
                                    //取消关注可写事件 5
                                    key.interestOps(key.interestOps() - SelectionKey.OP_WRITE);
                                    key.attach(null);
                                }
                            }
                        }

                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
