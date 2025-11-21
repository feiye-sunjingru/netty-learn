package com.feiye.netty1.bytebuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.extern.slf4j.Slf4j;

import static io.netty.buffer.ByteBufUtil.appendPrettyHexDump;
import static io.netty.util.internal.StringUtil.NEWLINE;

@Slf4j
public class TestBytebuf {
    public static void main(String[] args) {
        //默认池化.pooled|unpooled
        System.setProperty("io.netty.allocator.type", "pooled");

        //创建：ByteBufAllocator 默认256字节，可以自动扩容
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();

        buffer.writeBytes(new byte[]{1, 2, 3, 4});
        //log(buffer);

        buffer.writeInt(5);
        log(buffer);

        System.out.println( buffer);
        log(buffer);

        buffer.writeBytes("a".repeat(300).getBytes());
        log.debug("{}", buffer);
        log(buffer);
    }

    /**
     * 打印ByteBuf: 只打印尚未读取的内容
     *
     * @param buf
     */
    public static void log(ByteBuf buf) {
        int length = buf.readableBytes();
        int rows = length / 16 + (length % 15 == 0 ? 0 : 1) + 4;
        StringBuilder sb = new StringBuilder(rows * 80 * 2)
                .append("read index: ").append(buf.readerIndex())
                .append(" write index: ").append(buf.writerIndex())
                .append(" capacity: ").append(buf.capacity())
                .append(NEWLINE);
        appendPrettyHexDump(sb, buf);
        System.out.println(sb);
    }

}