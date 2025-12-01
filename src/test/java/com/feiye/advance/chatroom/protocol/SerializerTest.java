package com.feiye.advance.chatroom.protocol;

import com.feiye.advance.chatroom.config.Config;
import com.feiye.advance.chatroom.message.LoginRequestMessage;
import com.feiye.advance.chatroom.message.Message;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.logging.LoggingHandler;

class SerializerTest {
    public static void main(String[] args) {
        LoggingHandler loggingHandler = new LoggingHandler();
        MessageCodecSharable codec = new MessageCodecSharable();

        EmbeddedChannel channel = new EmbeddedChannel(loggingHandler, codec, loggingHandler);

        // 测试
        LoginRequestMessage message = new LoginRequestMessage("zhangsan", "123");

        // 出站，才能测试编码: 从后往前
        /*channel.writeOutbound(message);*/

        //入站只能测试解码（读）
        ByteBuf buf = messageToByteBuf(message);
        channel.writeInbound(buf);
    }

    public static ByteBuf messageToByteBuf(Message msg) {
        int algorithm = Config.getSerializerAlgorithm().ordinal();
        ByteBuf out = ByteBufAllocator.DEFAULT.buffer();
        out.writeBytes(new byte[]{1, 2, 3, 4});
        out.writeByte(1);
        out.writeByte(algorithm);
        out.writeByte(msg.getMessageType());
        out.writeInt(msg.getSequenceId());
        out.writeByte(0xff);
        byte[] bytes = Serializer.Algorithm.values()[algorithm].serialize(msg);
        out.writeInt(bytes.length);
        out.writeBytes(bytes);
        return out;
    }
}