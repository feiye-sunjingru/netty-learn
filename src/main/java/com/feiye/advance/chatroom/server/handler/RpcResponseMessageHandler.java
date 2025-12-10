package com.feiye.advance.chatroom.server.handler;

import com.feiye.advance.chatroom.message.RpcResponseMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * seqId=书包, 多线程访问使用ConcurrentHashMap
 */
@Slf4j
@ChannelHandler.Sharable
public class RpcResponseMessageHandler extends SimpleChannelInboundHandler<RpcResponseMessage> {
    //待处理的书包: 用于接收代理对象处理的结果; 也可以把PROMISES独立在这个类之外；为什么不能用Promise<?>泛型通配符:因为不能往里面放东西（可以放null），只能取值
    public static final ConcurrentHashMap<Integer, Promise<Object>> PROMISES = new ConcurrentHashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponseMessage msg) throws Exception {
        Exception exceptionValue = msg.getExceptionValue();
        Object returnValue = msg.getReturnValue();
        log.debug("response:{}", returnValue);
        // 获取到后，一定要移除PROMISE。不然MAP里面promise越来越多: 返回值并移出
        Promise<Object> promise = PROMISES.remove(msg.getSequenceId());
        if (exceptionValue != null) {
            promise.setFailure(msg.getExceptionValue());
        } else {
            promise.setSuccess(returnValue);
        }
    }
}
