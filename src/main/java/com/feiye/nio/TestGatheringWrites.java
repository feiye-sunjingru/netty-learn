package com.feiye.nio;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

import static com.feiye.nio.ByteBufferUtil.debugAll;


/**
 * Created by lilinchao
 * Date 2022/5/25
 * Description 集中写Demo
 */
public class TestGatheringWrites {
    public static void main(String[] args) {
        String prjRootPath = System.getProperty("user.dir");
        try (RandomAccessFile file = new RandomAccessFile(prjRootPath + "/helloworld/words.txt", "rw")) {
            FileChannel channel = file.getChannel();
            channel.position(11);

            ByteBuffer d = ByteBuffer.allocate(5);
            ByteBuffer e = ByteBuffer.allocate(5);
            d.put(StandardCharsets.UTF_8.encode("hello"));
            e.put(new byte[]{'w', 'o', 'r', 'l', 'd'});
            d.flip();
            e.flip();
            debugAll(d);
            debugAll(e);
            //utf-8中一个汉字占3个字节
            ByteBuffer f = ByteBuffer.wrap("你好".getBytes());
            channel.write(new ByteBuffer[]{d, e, f});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}