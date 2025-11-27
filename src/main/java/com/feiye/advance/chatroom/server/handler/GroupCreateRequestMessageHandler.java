package com.feiye.advance.chatroom.server.handler;

import com.feiye.advance.chatroom.message.GroupCreateRequestMessage;
import com.feiye.advance.chatroom.message.GroupCreateResponseMessage;
import com.feiye.advance.chatroom.server.session.Group;
import com.feiye.advance.chatroom.server.session.GroupSession;
import com.feiye.advance.chatroom.server.session.BaseGroupSessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.List;
import java.util.Set;

@ChannelHandler.Sharable
public class GroupCreateRequestMessageHandler extends SimpleChannelInboundHandler<GroupCreateRequestMessage> {
    /**
     * 读取创建群聊消息，返回结果并通知加入群聊用户信息
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupCreateRequestMessage msg) throws Exception {
        Set<String> members = msg.getMembers();
        String groupName = msg.getGroupName();

        GroupSession groupSession = BaseGroupSessionFactory.getGroupSession();
        Group group = groupSession.createGroup(groupName, members);
        if (group == null) {
            // 创建失败
            ctx.writeAndFlush(new GroupCreateResponseMessage(false, groupName + " 已经存在！群聊创建失败！"));
        } else {
            // 创建成功，返回结果并通知加入群聊用户信息
            ctx.writeAndFlush(new GroupCreateResponseMessage(true, groupName + " 创建成功！"));

            List<Channel> membersChannel = groupSession.getMembersChannel(groupName);
            membersChannel.forEach(channel -> {
                channel.writeAndFlush(new GroupCreateResponseMessage(true, "您已经拉入群聊" + groupName));
            });
        }
    }
}
