package com.feiye.nio.simplecs;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CountDownLatch;

import static com.feiye.nio.channelBuffer.ByteBufferUtil.debugAll;

/**
 * 文件AIO：参数路径、选项、线程池（多个线程，一个发送请求一个返回结果）
 * <p>
 * 异步IO：文件AIO、网络AIO
 * 多路复用：只有网络支持，即网络多路复用
 */
@Slf4j
public class AioFileChannel {
    public static void main(String[] args) {
        String prjRootPath = System.getProperty("user.dir");
        //路径、选项、线程池（多个线程，一个发送请求一个返回结果）
        try (AsynchronousFileChannel channel = AsynchronousFileChannel.open(Paths.get(prjRootPath + "/helloworld/words.txt"), StandardOpenOption.READ)) {
            AtomicInteger pos = new AtomicInteger(0);
            CountDownLatch latch = new CountDownLatch(1);

            ByteBuffer buffer = ByteBuffer.allocate(16);
            log.debug("read begin...");
            //用来返回结果的守护线程：随着主线程结束后，守护线程会结束
            //接收结果、读取起始位置、附件（怕读不完）、回调对象
            channel.read(buffer, 0, buffer, getHandler(pos, channel, latch));

            log.debug("read ended...");
            //注意：如果主线程不阻塞住可能没等到守护线程的回调结果，因此主线程会结束，守护线程的回调结果就不会打印
            latch.await();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static CompletionHandler<Integer, ByteBuffer> getHandler(AtomicInteger pos, AsynchronousFileChannel channel, CountDownLatch latch) {
        return new CompletionHandler<Integer, ByteBuffer>() {
            //当 result 为 -1 时正确终止递归并释放资源
            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                log.debug("read completed...{}", result);

                if (result != -1) {
                    attachment.flip();
                    log.debug("{}", new String(attachment.array()));
                    debugAll(attachment);

                    // 更新读取位置
                    pos.addAndGet(result);

                    attachment.clear();

                    // 继续读取下一个块
                    channel.read(attachment, pos.get(), attachment, this);
                } else {
                    // 读取完成，释放锁存器
                    log.debug("read ended...");
                    latch.countDown();
                }
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                exc.printStackTrace();
            }
        };
    }
}
