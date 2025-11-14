package com.feiye.nio;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static com.feiye.nio.ByteBufferUtil.debugAll;

public class TestByteBufferString {
    public static void main(String[] args) {
        //1.字符串转为ByteBuffer
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.put("hello".getBytes());
        debugAll(buffer);

        //2.CharSet实现字符串跟byteBuffer的转换
        ByteBuffer buffer1 = StandardCharsets.UTF_8.encode("你好");
        ByteBuffer buffer2 = Charset.forName("utf-8").encode("你好");

        debugAll(buffer1);
        debugAll(buffer2);

        CharBuffer buffer3 = StandardCharsets.UTF_8.decode(buffer1);
        System.out.println(buffer3.getClass());
        System.out.println(buffer3.toString());

        //3.wrap方法
        //注意，只有第二第三种可以这么decode，因为第一种还没有切换到读模式,什么也读不到,可以先调用buffer.flip();切换成读模式再sout
        ByteBuffer buffer4 = ByteBuffer.wrap("hello".getBytes());
        debugAll(buffer4);
        //buffer转字符串
        String str1 = StandardCharsets.UTF_8.decode(buffer4).toString();
        System.out.println(str1);
        buffer.flip();
        System.out.println(StandardCharsets.UTF_8.decode(buffer));
    }
}
