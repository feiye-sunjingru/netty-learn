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

/**
 * 可能存在的问题：
 * 1.单线程
 * 2.buffer大小不足以完全处理客户端数据
 * 3.这是来一个连接，就把所有连接中取一遍数据：很可能这里没数据
 * 4.不来连接，都无法进行已有 channel 读取
 * 这里3和4说明：读操作和接收连接操作互相影响
 */
@Slf4j
public class Server {
    public static void main(String[] args) throws IOException {
        // 使用 nio 来理解阻塞模式:单线程
        // 0. ByteBuffer
        ByteBuffer buffer = ByteBuffer.allocate(16);

        // 1. 创建了服务器对象：ServerSocketChannel会在没有连接建立时让线程暂停；
        ServerSocketChannel ssc = ServerSocketChannel.open();

        // 2. 绑定监听端口
        ssc.bind(new InetSocketAddress(8080));

        // 3. 连接集合：建立与客户端的连接：多个客户端
        List<SocketChannel> channels = new ArrayList<>();

        while (true) {
            // 4. accept 建立与客户端连接， SocketChannel 用来与客户端之间通信
            log.debug("connecting...");
            // 阻塞方法: 线程停止运行, 等待客户端连接: 在一个线程发送一次数据后需要接收新的连接才能进行下去
            // 通过 TCP的三次握手将连接建立起来
            // sc: 用于与客户端之间通信
            SocketChannel sc = ssc.accept();
            log.debug("connected... {}", sc);
            channels.add(sc);

            // 这是来一个连接，就把所有连接中取一遍数据：很可能这里没数据
            for (SocketChannel channel : channels) {
                // 5. 读取客户端发送的数据
                log.debug("before read... {}", channel);
                // 阻塞方法：线程停止运行：等待读入数据
                channel.read(buffer);
                buffer.flip();
                debugRead(buffer);
                buffer.clear();
                log.debug("after read... {}", channel);
            }
        }
    }
}
