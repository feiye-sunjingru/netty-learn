package com.feiye.nio;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

@Slf4j
public class ChannelDemo1 {
    public static <fis> void main(String[] args) {
        // 获取项目根路径
        String prjRootPath = System.getProperty("user.dir");
        // FileChannel
        // 1.输入输出流 2.RandomAccessFile
        //RandomAccessFile file = new RandomAccessFile(prjRootPath+"/helloworld/data.txt", "rw");
        //             FileChannel channel = file.getChannel()
        try (FileInputStream fis = new FileInputStream(prjRootPath+"/helloworld/data.txt");
             FileChannel channel = fis.getChannel()) {
            // 准备缓冲区
            ByteBuffer buffer = ByteBuffer.allocate(10);
            do {
                // 从channel 读取数据到 buffer
                int len = channel.read(buffer);
                log.debug("读到字节数：{}", len);
                // 读取完毕
                if (len == -1) {
                    break;
                }
                // 切换 buffer 读模式
                buffer.flip();
                while (buffer.hasRemaining()) {
                    log.debug("{}", (char) buffer.get());
                }
                // 切换 buffer 写模式：或使用.compact(
                buffer.clear();
            } while (true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}