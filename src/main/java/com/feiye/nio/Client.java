package com.feiye.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public class Client {
    public static void main(String[] args) throws IOException {
        //SocketChannel.read 会在没有数据可读时让线程暂停
        try (SocketChannel sc = SocketChannel.open()) {
            sc.connect(new InetSocketAddress("localhost", 8080));
            //sc.write(StandardCharsets.UTF_8.encode("mygod"))
            System.out.println("waiting...");
        }
    }
}
