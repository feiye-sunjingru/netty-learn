package com.feiye.nio.simplecs;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import static com.feiye.nio.channelBuffer.ByteBufferUtil.debugRead;

@Slf4j
public class ServerNIO {
    public static void main(String[] args) throws IOException {
        // 使用 nio 来理解非阻塞模式:单线程
        // 0. ByteBuffer
        ByteBuffer buffer = ByteBuffer.allocate(16);

        // 1. 创建了服务器：ServerSocketChannel会在没有连接建立时让线程暂停；
        ServerSocketChannel ssc = ServerSocketChannel.open();
        // 配置为非阻塞模式: 默认阻塞
        ssc.configureBlocking(false);

        // 2. 绑定监听端口
        ssc.bind(new InetSocketAddress(8080));

        // 3. 连接集合：建立与客户端的连接：多个客户端
        List<SocketChannel> channels = new ArrayList<>();

        while (true) {
            // 4. accept 建立与客户端连接， SocketChannel 用来与客户端之间通信：非阻塞单线程：大量空轮询
//            log.debug("connecting...");
            // ssc配置后非阻塞：如果没有连接建立，则返回null,一直空轮询
            SocketChannel sc = ssc.accept();
            if (sc != null) {
                log.debug("connected... {}", sc);
                // 影响read方法
                sc.configureBlocking(false);
                channels.add(sc);
            }

            for (SocketChannel channel : channels) {
                // 5. 接收客户端发送的数据
//                log.debug("before read... {}", channel);
                // 非阻塞方法：线程仍然运行，没有数据可读时，返回0
                int read = channel.read(buffer);
                if (read > 0) {
                    // 读到了数据
                    buffer.flip();
                    debugRead(buffer);
                    buffer.clear();
                    log.debug("after read... {}", channel);
                }
            }
        }
    }
}
