package com.feiye.advance.chatroom.protocol;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class ProcotolFrameDecoder extends LengthFieldBasedFrameDecoder {

    // 最大长度超过1024会抛出TooLongFrameException
    public ProcotolFrameDecoder() {
        this(1024, 12, 4, 0, 0);
    }

    public ProcotolFrameDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }
}
