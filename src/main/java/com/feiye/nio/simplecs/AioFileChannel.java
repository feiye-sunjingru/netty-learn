package com.feiye.nio.simplecs;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static com.feiye.nio.channelBuffer.ByteBufferUtil.debugAll;

@Slf4j
public class AioFileChannel {
    public static void main(String[] args) {
        String prjRootPath = System.getProperty("user.dir");
        try (AsynchronousFileChannel channel = AsynchronousFileChannel.open(Paths.get(prjRootPath + "/helloworld/words.txt"), StandardOpenOption.READ)) {
            // bytebuffer、读取的起始位置、附件（一次读不完接着读）、回调对象
            ByteBuffer buffer = ByteBuffer.allocate(16);
            log.debug("read begin...");
            //用来返回结果的守护线程：随着主线程结束后，守护线程会结束
            channel.read(buffer, 0, buffer, new CompletionHandler<Integer, ByteBuffer>() {
                //一次读取成功
                @Override
                public void completed(Integer result, ByteBuffer attachment) {
                    log.debug("read completed...{}", result);
                    attachment.flip();
                    //还没等到其他线程把结果送来，主线程就结束了，因此没有打印
                    debugAll(attachment);
                }

                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    exc.printStackTrace();
                }
            });

            log.debug("read ended...");
            //注意：如果主线程不阻塞住可能没等到守护线程的回调结果，因此主线程会结束，守护线程的回调结果就不会打印
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
