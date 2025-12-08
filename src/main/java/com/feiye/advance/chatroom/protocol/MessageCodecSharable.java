package com.feiye.advance.chatroom.protocol;

import com.feiye.advance.chatroom.config.Config;
import com.feiye.advance.chatroom.message.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@ChannelHandler.Sharable
/**
 * 必须和 LengthFieldBasedFrameDecoder 一起使用，确保接到的 ByteBuf 消息是完整的
 * 注意：这里protobuf老师尝试过，在这里不太好使
 */
public class MessageCodecSharable extends MessageToMessageCodec<ByteBuf, Message> {
    /**
     * 将Message对象转换为ByteBuf对象（前几个字节添加了额外的信息）
     *
     * @param ctx
     * @param msg
     * @param outList
     * @throws Exception
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> outList) throws Exception {
//        System.out.println("编码。。。");
        ByteBuf out = ctx.alloc().buffer();
        // 1. 4 字节的魔数
        out.writeBytes(new byte[]{1, 2, 3, 4});
        // 2. 1 字节的版本,
        out.writeByte(1);
        // 3. 1 字节的序列化方式 jdk 0 , json 1
        out.writeByte(Config.getSerializerAlgorithm().ordinal());
        // 4. 1 字节的指令类型
        out.writeByte(msg.getMessageType());
        // 5. 4 个字节
        out.writeInt(msg.getSequenceId());
        // 无意义，对齐填充
        out.writeByte(0xff);
        // 6. 获取内容的字节数组
        byte[] bytes = Config.getSerializerAlgorithm().serialize(msg);
        // 7. 长度
        out.writeInt(bytes.length);
        // 8. 写入内容
        out.writeBytes(bytes);

        outList.add(out);
        log.debug("encode:{}", msg);
    }

    /**
     * ByteBuf对象（前几个字节添加了额外的信息）转换为Message对象
     *
     * @param ctx
     * @param in
     * @param out
     * @throws Exception
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int magicNum = in.readInt();
        byte version = in.readByte();
        // 序列化类型
        byte serializerType = in.readByte();
        //具体的消息类型
        byte messageType = in.readByte();
        int sequenceId = in.readInt();
        in.readByte();
        int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readBytes(bytes, 0, length);
        //找出反序列化算法
        Serializer.Algorithm serializerAlgorithm = Serializer.Algorithm.values()[serializerType];
        //找出具体消息类型
        Class<? extends Message> messageClass = Message.getMessageClass(messageType);
        //需要根据具体消息类型进行反序列化
        Message message = serializerAlgorithm.deserialize(messageClass, bytes);
        log.debug("magicNum：{}, version：{}, serializerType：{}, messageType：{}, sequenceId：{}, length:{}", magicNum, version, serializerType, messageType, sequenceId, length);
//        log.debug("decode:{}", message);
        out.add(message);
    }
}
