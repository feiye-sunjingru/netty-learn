package com.feiye.netty1.bytebuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import static com.feiye.netty1.bytebuf.TestBytebuf.log;

public class TestSlice {
    public static void main(String[] args) {
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer(10);
        byteBuf.writeBytes(new byte[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j'});
        log(byteBuf);
        // 在切片的过程中没有发生数据的复制: index length
        ByteBuf buf1 = byteBuf.slice(0, 5);
        ByteBuf buf2 = byteBuf.slice(5, 5);
        log(buf1);
        log(buf2);

        System.out.println("==========================================");
        // 测试是否是同一块内存
        buf1.setByte(0, 'x');
        log(byteBuf);
        log(buf1);
        // 切片后，最大容量做了限制 IndexOutOfBoundsException
        //buf1.writeByte('s');

        /*System.out.println("释放原有bytebuf内存");
        byteBuf.release();
        //释放之后buf1不可用
        log(buf1);*/

        // 先存着
        System.out.println("**************************************");
        buf1.retain();
        buf2.retain();

        byteBuf.release();
        //log(buf1);
        //byteBuf还在
        //log(byteBuf);

        // 注意：用完之后需要自己释放
        buf1.release();
        //log(byteBuf);
        buf2.release();
        //byteBuf被释放了:这里会报错
        log(byteBuf);
    }
}
