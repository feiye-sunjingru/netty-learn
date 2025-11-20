package com.feiye.nio.simplecs;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

@Slf4j
public class WriteClient {
    public static void main(String[] args) throws IOException {
        SocketChannel sc = SocketChannel.open();
        sc.connect(new InetSocketAddress("localhost", 8080));

        //接收服务端数据：一次1Mb
        ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
        int count = 0;
        while (true) {
            int read = sc.read(buffer);
            count += read;
            log.debug("read...{}", count);
            if (read == -1) {
                break;
            }
            buffer.clear();
        }
        System.out.println("over");
    }
}
