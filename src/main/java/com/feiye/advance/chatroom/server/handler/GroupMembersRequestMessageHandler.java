package com.feiye.advance.chatroom.server.handler;

import com.feiye.advance.chatroom.message.GroupMembersRequestMessage;
import com.feiye.advance.chatroom.message.GroupMembersResponseMessage;
import com.feiye.advance.chatroom.server.session.BaseGroupSessionFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Description 查看成员Handler
 */
@ChannelHandler.Sharable
public class GroupMembersRequestMessageHandler extends SimpleChannelInboundHandler<GroupMembersRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupMembersRequestMessage msg) throws Exception {
        ctx.writeAndFlush(new GroupMembersResponseMessage(BaseGroupSessionFactory.getGroupSession().getMembers(msg.getGroupName())));
    }
}
