package com.feiye.nio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

@Slf4j
public class WriteServer {
    public static void main(String[] args) throws IOException {
        //生成服务端Channel
        ServerSocketChannel ssc = ServerSocketChannel.open();
        // 绑定端口
        ssc.bind(new InetSocketAddress(8080));
        // 生成selector监听这些Channel
        Selector selector = Selector.open();
        ssc.configureBlocking(false);
        SelectionKey sscKey = ssc.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            selector.select();

            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();

                if (key.isAcceptable()) {
                    SocketChannel sc = ssc.accept();
                    sc.configureBlocking(false);
                    //1
                    SelectionKey scKey = sc.register(selector, SelectionKey.OP_READ);

                    //向客户端写入大量数据
                    ByteBuffer buffer = StandardCharsets.UTF_8.encode("a".repeat(10000000));
                    int write = sc.write(buffer);
                    //网络发送能力有限：网络缓冲区满，这里会返回0
                    log.debug("write...{}", write);
                    if (buffer.hasRemaining()) {
                        //关注可写事件 4
                        scKey.interestOps(scKey.interestOps() + SelectionKey.OP_WRITE);
                        scKey.attach(buffer);
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

        }
    }
}
