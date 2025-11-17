package com.feiye.nio.file;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class TestFileChannelTransferTo {
    public static void main(String[] args) {
        String prjRootPath = System.getProperty("user.dir");
        // 源文件路径
        String FROM = prjRootPath + "/helloworld/data.txt";
        // 目标文件路径
        String TO = prjRootPath + "/helloworld/to.txt";
        // 记录开始时间
        long start = System.nanoTime();

        //;分隔：多个资源管理
        try (FileChannel from = new FileInputStream(FROM).getChannel();
             FileChannel to = new FileOutputStream(TO).getChannel();) {
            // 通过 transferTo 方法传输数据：效率高，底层会利用操作系统的零拷贝进行优化
            //一次只能传输2G数据
            long size = from.size();
            // left表示还剩多少字节
            for (long left = size; left > 0; ) {
                System.out.println("position:" + (size - left) + " left:" + left);
                left -= from.transferTo(size - left, left, to);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        long end = System.nanoTime();
        System.out.println("transferTo 用时：" + (end - start) / 1000_000.0);
    }
}
