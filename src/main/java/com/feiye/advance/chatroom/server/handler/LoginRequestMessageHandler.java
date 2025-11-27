package com.feiye.advance.chatroom.server.handler;

import com.feiye.advance.chatroom.message.LoginRequestMessage;
import com.feiye.advance.chatroom.message.LoginResponseMessage;
import com.feiye.advance.chatroom.server.service.BaseUserServiceFactory;
import com.feiye.advance.chatroom.server.session.BaseSessionFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable
public class LoginRequestMessageHandler extends SimpleChannelInboundHandler<LoginRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LoginRequestMessage msg) throws Exception {
        // 读取消息
        String username = msg.getUsername();
        String password = msg.getPassword();
        // 业务逻辑：根据用户名密码返回登录结果
        boolean login = BaseUserServiceFactory.getUserService().login(username, password);
        // 返回结果
        LoginResponseMessage loginResp;
        if (login) {
            // 登录成功了，需要将channel和用户名绑定起来
            BaseSessionFactory.getSession().bind(ctx.channel(), username);
            loginResp = new LoginResponseMessage(true, "登录成功！");
        } else {
            loginResp = new LoginResponseMessage(false, "用户名或者密码错误！");
        }

        ctx.writeAndFlush(loginResp);
    }
}
