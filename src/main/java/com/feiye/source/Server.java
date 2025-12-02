package com.feiye.source;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) throws IOException {
        try(ServerSocket ss = new ServerSocket(8888, 2)){
            Socket accept = ss.accept();
            System.out.println(accept);
            System.in.read();
        }
    }
}