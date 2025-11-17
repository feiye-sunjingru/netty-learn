package com.feiye.nio.channelBuffer;

import java.nio.ByteBuffer;

import static com.feiye.nio.channelBuffer.ByteBufferUtil.debugAll;

/**
 * 网络上有多条数据发送给服务端，数据之间使用\n进行分隔
 * 但由于某种原因这些数据在接收时，被进行了重新组合，例如原始数据有3条为
 * Hello,world\n
 * I'm zhangsan\n
 * How are you?\n
 * 变成了下面的两个 byteBuffer
 * Hello,world \nI'm zhangsan\nHo（合并在一起：黏包，效率高）
 * w are you?\n（被截断：半包）
 * 现在要求你编写程序，将错乱的数据恢复成原始的按\n分隔的数据
 */
public class TestByteBufferExam {
    public static void main(String[] args) {
        ByteBuffer source = ByteBuffer.allocate(32);
        source.put("Hello,world\nI'm zhangsan\nHo".getBytes());
        split(source);
        source.put("w are you?\n".getBytes());
        split(source);
    }

    private static void split(ByteBuffer source) {
        source.flip();

        for (int i = 0; i < source.limit(); i++) {
            //找到一条完整消息
            if (source.get(i) == '\n') {
                int length = i + 1 - source.position();
                //完整的消息存入新的byteBuffer
                ByteBuffer target = ByteBuffer.allocate(length);
                for (int j = 0; j < length; j++) {
                    //source读，target写
                    target.put(source.get());
                }
                debugAll(target);
            }
        }

        // 没读完的放在下次再继续读
        source.compact();
    }
}
