package com.feiye.advance.protocol.udf;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * 自定义协议进行编解码：父类ByteToMessageCodec
 * ByteToMessageCodec子类不允许Sharable
 */
@Slf4j
public class MessageCodec extends ByteToMessageCodec<Message> {
    // 编码成bytebuf
    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        // 1、 4 字节的魔数
        out.writeBytes(new byte[]{1, 2, 3, 4});
        // 2、 1 字节的版本
        out.writeByte(1);
        // 3、 1 字节的序列化方式：jdk 0, json 1, protobuf, thrift, avro, kryo, hessian
        out.writeByte(0);
        // 4、 1 字节的指令类型
        out.writeByte(msg.getMessageType());
        // 5、 4 字节的请求序号
        out.writeInt(msg.getSequenceId());
        // 无意义，对齐填充
        out.writeByte(0xff);
        // 6、 对象转byte数组：获取内容msg的字节数组
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(msg);
        byte[] bytes = bos.toByteArray();
        // 7、 4 字节的长度
        out.writeInt(bytes.length);
        // 8、 写入内容：解码结果给下一个handler用
        out.writeBytes(bytes);
        // 4 + 1 + 1 + 1 + 1 + 4 + 4 = 16 字节，控制在 2^n ，可以添加无意义的字节
    }

    // 解码成Message
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int magicNum = in.readInt();
        byte version = in.readByte();
        byte serializerType = in.readByte();
        byte messageType = in.readByte();
        int sequenceId = in.readInt();
        in.readByte();
        int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readBytes(bytes, 0, length);
        Message msg = null;
        if (serializerType == 0) {
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
            msg = (Message) ois.readObject();
        }
        log.debug("{}, {}, {}, {}, {}, {}", magicNum, version, serializerType, messageType, sequenceId, length);
        log.debug("{}", msg);

        out.add(msg);
    }
}
