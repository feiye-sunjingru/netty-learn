package com.feiye.advance;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class TestLengthFieldDecoder {
    public static void main(String[] args) {
        //测试编解码
        EmbeddedChannel channel = new EmbeddedChannel(
                //定义好格式后进行解析：读完内容之后再进行后续循环解析.最后initialBytesToStrip表示剥离前面几个字节后显示
                new LengthFieldBasedFrameDecoder(1024, 0, 4, 1, 5),
                new LoggingHandler(LogLevel.DEBUG)
        );
        // 4 个字节的内容长度， 实际内容
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer();
        send(byteBuf, "Hello, World");
        send(byteBuf, "Hi!");
        channel.writeInbound(byteBuf);
    }

    private static void send(ByteBuf byteBuf, String content) {
        // 实际内容
        byte[] bytes = content.getBytes();
        // 实际内容长度
        int length = bytes.length;
        byteBuf.writeInt(length);
        // 例如版本号
        byteBuf.writeByte(1);
        byteBuf.writeBytes(bytes);
    }
}
