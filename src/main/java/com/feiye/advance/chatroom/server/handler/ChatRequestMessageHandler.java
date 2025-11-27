package com.feiye.advance.chatroom.server.handler;

import com.feiye.advance.chatroom.message.ChatRequestMessage;
import com.feiye.advance.chatroom.message.ChatResponseMessage;
import com.feiye.advance.chatroom.server.session.BaseSessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * 接收信息类型是ChatRequestMessage时触发
 */
@ChannelHandler.Sharable
@Slf4j
public class ChatRequestMessageHandler extends SimpleChannelInboundHandler<ChatRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ChatRequestMessage msg) throws Exception {
        try {
            System.out.println("读chatRequest消息");
            String to = msg.getTo();
            String content = msg.getContent();
            Channel toChannel = BaseSessionFactory.getSession().getChannel(to);
//            log.debug("拿到channel{}",toChannel.toString());
            if (toChannel != null) {
                // 在线
                toChannel.writeAndFlush(new ChatResponseMessage(msg.getFrom(), content));
            } else {
                // 不在线，给发送者返回消息
                ctx.channel().writeAndFlush(new ChatResponseMessage(false, "您发送的用户不存在或者不在线"));
            }

        } catch (Exception e) {
            log.debug("{}", e);
        }


    }
}
