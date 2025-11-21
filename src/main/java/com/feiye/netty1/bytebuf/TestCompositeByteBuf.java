package com.feiye.netty1.bytebuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;

import static com.feiye.netty1.bytebuf.TestBytebuf.log;

public class TestCompositeByteBuf {
    public static void main(String[] args) {
//        testComposite();
        testUnpooled();
    }

    private static void testComposite() {
        ByteBuf buf1 = ByteBufAllocator.DEFAULT.buffer();
        ByteBuf buf2 = ByteBufAllocator.DEFAULT.buffer();
        buf1.writeBytes(new byte[]{1, 2, 3, 4, 5});
        buf1.writeBytes(new byte[]{6, 7, 8, 9, 10});

        // 传统的: 发生了多次数据复制影响性能
        /*ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        buffer.writeBytes(buf1).writeBytes(buf2);
        log(buffer);*/

        // composite：逻辑上组到一起
        CompositeByteBuf compositeBuffer = ByteBufAllocator.DEFAULT.compositeBuffer();
        //默认不会调整写指针位置，这里设为true进行自动调整
        compositeBuffer.addComponents(true, buf1, buf2);
        log(compositeBuffer);
    }

    private static void testUnpooled() {
        ByteBuf buf1 = ByteBufAllocator.DEFAULT.buffer();
        ByteBuf buf2 = ByteBufAllocator.DEFAULT.buffer();
        buf1.writeBytes(new byte[]{1, 2, 3, 4, 5});
        buf1.writeBytes(new byte[]{6, 7, 8, 9, 10});

        // 当包装 ByteBuf 个数超过一个时，底层使用了 CompositeByteBuf
        ByteBuf byteBuf = Unpooled.wrappedBuffer(buf1, buf2);
        System.out.println(byteBuf.getClass());
        log(byteBuf);
    }

}
