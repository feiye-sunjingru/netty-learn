package com.feiye.nio.channelBuffer;

import java.nio.ByteBuffer;

import static com.feiye.nio.channelBuffer.ByteBufferUtil.debugAll;

//各种方法
public class TestByteBufferRead {
    public static void main(String[] args) {
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.put(new byte[]{'a', 'b', 'c', 'd'});
        buffer.flip();

        //get方法会让position指针向后移，
        // 如果想读重复数据，可以用rewind方法使position重置为0，或者调用get(int i)获取索引i处数据，但不会移动指针
        /*buffer.get(new byte[4]);
        debugAll(buffer);
        buffer.rewind();
        System.out.println((char)buffer.get());*/

        //mark & reset: mark 做一个标记，记录position的位置，reset是将position重置到mark的位置。
        /*System.out.println((char) buffer.get());
        System.out.println((char) buffer.get());
        //加标记，于索引为2的位置
        buffer.mark();
        System.out.println((char) buffer.get());
        System.out.println((char) buffer.get());
        buffer.reset();//将position重置到索引2
        System.out.println((char) buffer.get());
        System.out.println((char) buffer.get());*/

        //get(i)但不会移动指针位置
        System.out.println((char) buffer.get(3));
        debugAll(buffer);
        System.out.println(buffer);
    }
}