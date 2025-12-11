package com.feiye.nio.channelBuffer;

import java.nio.ByteBuffer;

/**
 * nio heap ByteBuffer
 */
public class TestByteBuffer {
    public static void main(String[] args) {
        ByteBuffer buffer = ByteBuffer.allocate(10);
        //java.nio.HeapByteBuffer
        System.out.println(buffer.getClass());

        // 向buffer中写入1个字节的数据
        buffer.put((byte) 0x61);
        // 使用工具类，查看buffer状态
        ByteBufferUtil.debugAll(buffer);

        // 向buffer中写入4个字节的数据
        buffer.put(new byte[]{0x62, 0x63, 0x64, 0x65});
        ByteBufferUtil.debugAll(buffer);

        // 获取数据
        buffer.flip();
        ByteBufferUtil.debugAll(buffer);
        // 指针移动
        System.out.println(buffer.get());
        System.out.println(buffer.get());
        ByteBufferUtil.debugAll(buffer);
        System.out.println("**************************************");

        // 使用compact切换buffer写模式：将未读数据（position 到 limit 之间）复制到缓冲区开头
        // position 设置为未读数据的长度
        buffer.compact();
        ByteBufferUtil.debugAll(buffer);

        // 再次写入
        buffer.put((byte) 102);
        buffer.put((byte) 103);
        ByteBufferUtil.debugAll(buffer);
    }
}