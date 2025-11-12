package com.feiye.nio;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

/**
 * Java NIO 写入性能测试代码：传统 BufferedOutputStream vs NIO FileChannel（直接内存 & 堆内存）
 * [Traditional IO] Time: 1.76 s, Throughput: 582.48 MB/s
 * [NIO Heap Buffer] Time: 1.70 s, Throughput: 603.42 MB/s
 * [NIO Direct Buffer] Time: 1.71 s, Throughput: 598.48 MB/s
 */
public class NIOWritePerformanceTest {
    /**
     * 1GB 文件
     */
    private static final long FILE_SIZE = (long) 10 * 1024 * 1024 * 1024;
    /**
     * 缓冲区大小: 8KB
     */
    private static final int BUFFER_SIZE = 8 * 1024;

    public static void main(String[] args) throws IOException {
        byte[] dummyData = new byte[BUFFER_SIZE];
        // 填充固定数据
        Arrays.fill(dummyData, (byte) 'A');

        // 测试传统 IO
        testTraditionalIO(dummyData);

        // 测试 NIO（堆内 ByteBuffer）
        testNIOHeap(dummyData);

        // 测试 NIO（直接 ByteBuffer）
        testNIODirect(dummyData);
    }

    /**
     * 传统 IO：BufferedOutputStream
     *
     * @param data
     * @throws IOException
     */
    private static void testTraditionalIO(byte[] data) throws IOException {
        String filePath = "test_traditional.bin";
        long start = System.currentTimeMillis();

        try (FileOutputStream fos = new FileOutputStream(filePath);
             BufferedOutputStream bos = new BufferedOutputStream(fos, BUFFER_SIZE)) {
            long written = 0;
            while (written < FILE_SIZE) {
                int toWrite = (int) Math.min(data.length, FILE_SIZE - written);
                bos.write(data, 0, toWrite);
                written += toWrite;
            }
            bos.flush();
        }

        long end = System.currentTimeMillis();
        double seconds = (end - start) / 1000.0;
        double throughput = (FILE_SIZE / 1024.0 / 1024.0) / seconds;
        System.out.printf("[Traditional IO] Time: %.2f s, Throughput: %.2f MB/s%n", seconds, throughput);

        // 清理
        new File(filePath).delete();
    }

    /**
     * NIO：堆内 ByteBuffer
     *
     * @param data
     * @throws IOException
     */
    private static void testNIOHeap(byte[] data) throws IOException {
        String filePath = "test_nio_heap.bin";
        long start = System.currentTimeMillis();

        //2.创建 ByteBuffer 实例并填充数据
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        buffer.put(data);
        // 将 ByteBuffer 的模式从写切换到读，以便能够读取数据
        buffer.flip();

        //1.创建文件通道（FileChannel）
        try (FileChannel channel = FileChannel.open(java.nio.file.Paths.get(filePath),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            long written = 0;
            while (written < FILE_SIZE) {
                // 重置 position=0, limit=capacity
                buffer.rewind();
                int toWrite = (int) Math.min(buffer.remaining(), FILE_SIZE - written);
                // 防止写超
                buffer.limit(toWrite);
                while (buffer.hasRemaining()) {
                    //3.将 ByteBuffer 写入到文件通道中
                    channel.write(buffer);
                }
                written += toWrite;
            }
        }
        //4.关闭文件通道
        long end = System.currentTimeMillis();
        double seconds = (end - start) / 1000.0;
        double throughput = (FILE_SIZE / 1024.0 / 1024.0) / seconds;
        System.out.printf("[NIO Heap Buffer] Time: %.2f s, Throughput: %.2f MB/s%n", seconds, throughput);

        new File(filePath).delete();
    }

    /**
     * NIO：直接内存 ByteBuffer（Direct Buffer）
     *
     * @param data
     * @throws IOException
     */
    private static void testNIODirect(byte[] data) throws IOException {
        String filePath = "test_nio_direct.bin";
        long start = System.currentTimeMillis();

        ByteBuffer directBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
        directBuffer.put(data);
        directBuffer.flip();

        try (FileChannel channel = FileChannel.open(java.nio.file.Paths.get(filePath),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            long written = 0;
            while (written < FILE_SIZE) {
                directBuffer.rewind();
                int toWrite = (int) Math.min(directBuffer.remaining(), FILE_SIZE - written);
                directBuffer.limit(toWrite);
                while (directBuffer.hasRemaining()) {
                    channel.write(directBuffer);
                }
                written += toWrite;
            }
        }

        long end = System.currentTimeMillis();
        double seconds = (end - start) / 1000.0;
        double throughput = (FILE_SIZE / 1024.0 / 1024.0) / seconds;
        System.out.printf("[NIO Direct Buffer] Time: %.2f s, Throughput: %.2f MB/s%n", seconds, throughput);

        new File(filePath).delete();
    }
}