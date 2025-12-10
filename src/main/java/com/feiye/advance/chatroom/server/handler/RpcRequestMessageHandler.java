package com.feiye.advance.chatroom.server.handler;

import com.feiye.advance.chatroom.message.RpcRequestMessage;
import com.feiye.advance.chatroom.message.RpcResponseMessage;
import com.feiye.advance.chatroom.server.service.HelloService;
import com.feiye.advance.chatroom.server.service.ServicesFactory;
import com.feiye.advance.chatroom.utils.RpcException;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

/**
 * RPC 请求处理器: 通过反射机制调用方法
 */
@Slf4j
@ChannelHandler.Sharable
public class RpcRequestMessageHandler extends SimpleChannelInboundHandler<RpcRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequestMessage msg) {
        RpcResponseMessage rpcResp = new RpcResponseMessage();
        //要求请求和响应的sequenceId 一致
        rpcResp.setSequenceId(msg.getSequenceId());
        HelloService service = null;
        try {
            //1.获取实现类的Class对象
            service = (HelloService) ServicesFactory.getService(Class.forName(msg.getInterfaceName()));
            //2.获取接口中的方法：方法名、参数类型
            Method method = service.getClass().getMethod(msg.getMethodName(), msg.getParameterTypes());
            //3.调用实现类，传入参数
            Object invoke = method.invoke(service, msg.getParameterValue());
            rpcResp.setReturnValue(invoke);
        } catch (Exception e) {
            //注意：这里自定义异常是为了减少输出的内容
            rpcResp.setExceptionValue(new RpcException("远程调用出错" + e.getCause().getMessage()));
            e.printStackTrace();
        }
        log.debug("server send msg");
        ctx.writeAndFlush(rpcResp);
    }

    public static void main(String[] args) {
        RpcRequestMessage rpcRequestMessage = new RpcRequestMessage(
                1,
                //复制接口引用即可
                "com.feiye.advance.chatroom.server.service.HelloService",
                "sayHello",
                String.class,
                new Class[]{String.class},
                new Object[]{"张三"});
        try {
            HelloService service = (HelloService) ServicesFactory.getService(Class.forName(rpcRequestMessage.getInterfaceName()));
            Method method = service.getClass().getMethod(rpcRequestMessage.getMethodName(), rpcRequestMessage.getParameterTypes());
            String invoke = (String) method.invoke(service, rpcRequestMessage.getParameterValue());
            System.out.println(invoke);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
