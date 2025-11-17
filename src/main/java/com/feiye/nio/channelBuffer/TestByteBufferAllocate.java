package com.feiye.nio.channelBuffer;

import java.nio.ByteBuffer;

public class TestByteBufferAllocate {
    public static void main(String[] args) {
        // 1.分配一个指定大小的缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(10);

        // 2.allocateDirect()分配直接缓冲区
        ByteBuffer directBuffer = ByteBuffer.allocateDirect(10);

        /*
        * class java.nio.HeapByteBuffer java堆内存，读写效率较低；受到GC影响（拷贝或移动以便更紧凑，影响效率）
        * class java.nio.DirectByteBuffer 直接内存，读写效率高（少一次拷贝）；不会受GC影响；分配速度较慢；使用不当可能会内存泄露
        * */
        System.out.println(buffer.getClass());
        System.out.println(directBuffer.getClass());
    }
}
