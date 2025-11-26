package com.feiye.advance.protocol.udf;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 子类知道自己的消息类型
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
public class LoginRequestMessage extends Message {
    private String username;
    private String password;
    private String nickname;

    public LoginRequestMessage() {
    }

    public LoginRequestMessage(String username, String password, String nickname) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
    }

    @Override
    public int getMessageType() {
        return LoginRequestMessage;
    }
}
