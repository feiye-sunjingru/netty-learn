package com.feiye.nio;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import static com.feiye.nio.ByteBufferUtil.debugAll;

/**
 * Created by lilinchao
 * Date 2022/5/25
 * Description 分散读 Demo
 */
public class TestScatteringReads {
    public static void main(String[] args) {
        String prjRootPath = System.getProperty("user.dir");
        try (RandomAccessFile fis = new RandomAccessFile(prjRootPath + "/helloworld/words.txt", "r");
             FileChannel channel = fis.getChannel()) {
            ByteBuffer b1 = ByteBuffer.allocate(3);
            ByteBuffer b2 = ByteBuffer.allocate(3);
            ByteBuffer b3 = ByteBuffer.allocate(5);
            channel.read(new ByteBuffer[]{b1, b2, b3});
            b1.flip();
            b2.flip();
            b3.flip();
            debugAll(b1);
            debugAll(b2);
            debugAll(b3);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}