package com.feiye.advance.protocol.udf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * 通过LengthFieldBasedFrameDecoder解决粘包半包问题：没读完不处理，等待下次读
 * 包含@Sharable已经充分考虑了线程安全，一个实例就够：底层就是不保存状态信息：线程安全
 * 一般解码器都是不能被线程共享的
 */
public class TestMessageCodec {
    public static void main(String[] args) throws Exception {
        //不保存状态信息：线程安全：包含@Sharable已经充分考虑了线程安全，一个实例就够
        LoggingHandler loggingHandler = new LoggingHandler(LogLevel.DEBUG);
        //工人i使用这个：需要等待结果: 会出现并发问题（记录了当前线程的状态，就是线程不安全的）
        //LengthFieldBasedFrameDecoder fieldBasedFrameDecoder = new LengthFieldBasedFrameDecoder(1024, 12, 4, 0, 0);
        MessageCodecSharable messageCodecSharable = new MessageCodecSharable();

        EmbeddedChannel channel = new EmbeddedChannel(
                loggingHandler,
                //需要解决粘包半包问题：半包会导致decode错误
                new LengthFieldBasedFrameDecoder(1024, 12, 4, 0, 0),
                messageCodecSharable //new MessageCodec()
        );
        // 消息正文
        LoginRequestMessage message = new LoginRequestMessage("mianbao", "admin", "面包");
        // 出站编码:channel写，encode方法起作用，将message对象转为ByteBuf
        /*channel.writeOutbound(message);*/

        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        // 编码 encode： protected方法，此类必须和 MessageCodec 类在同一个包下
        new MessageCodec().encode(null, message, buf);
        // 模拟半包
        // 入站:需要bytebuf, channel读入
        /*channel.writeInbound(buf);*/

        // 模拟半包错误：
        ByteBuf s1 = buf.slice(0, 100);
        ByteBuf s2 = buf.slice(100, buf.readableBytes() - 100);
        s1.retain();
        // channel.writeInbound会调用一次buf.release
        channel.writeInbound(s1);
        channel.writeInbound(s2);
    }
}
