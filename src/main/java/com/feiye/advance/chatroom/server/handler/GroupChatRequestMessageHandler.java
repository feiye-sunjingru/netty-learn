package com.feiye.advance.chatroom.server.handler;

import com.feiye.advance.chatroom.message.GroupChatRequestMessage;
import com.feiye.advance.chatroom.message.GroupChatResponseMessage;
import com.feiye.advance.chatroom.server.session.BaseGroupSessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 处理群聊消息，将群聊消息发送到对应的用户。
 */
@ChannelHandler.Sharable
@Slf4j
public class GroupChatRequestMessageHandler extends SimpleChannelInboundHandler<GroupChatRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupChatRequestMessage msg) throws Exception {
        String groupName = msg.getGroupName();

        List<Channel> membersChannel = BaseGroupSessionFactory.getGroupSession().getMembersChannel(groupName);

        for (Channel channel : membersChannel) {
            channel.writeAndFlush(new GroupChatResponseMessage(msg.getFrom(), msg.getContent()));
        }
        if(membersChannel.isEmpty()){
            ctx.writeAndFlush(new GroupChatResponseMessage(false, "组名不存在！"));
        }else {
            ctx.writeAndFlush(new GroupChatResponseMessage(true, "发送群聊消息成功！"));
        }
    }
}
