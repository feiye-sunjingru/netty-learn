package com.feiye.advance.protocol.udf;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 消息的公共父类
 */
public abstract class Message implements Serializable {

    private int SequenceId;

    public abstract int getMessageType();

    public int getSequenceId() {
        return SequenceId;
    }

    public static final int LoginRequestMessage = 0;
    public static final int LoginResponseMessage = 1;
    public static final int chatRequestMessage = 2;
    public static final int ChatResponseMessage = 3;
    public static final int GroupCreateReguestMessage = 4;
    public static final int GroupCreateResponseMessage = 5;
    public static final int GroupJoinRequestMessage = 6;
    public static final int GroupJoinResponseMessage = 7;
    public static final int GroupQuitRequestMessage = 8;
    public static final int GroupQuitResponseMessage = 9;
    public static final int GroupchatRequestMessage = 10;
    public static final int GroupChatResponseMessage = 11;
    public static final int GroupMembersRequestMessage = 12;
    public static final int GroupMembersResponseMessage = 13;
    private static final Map<Integer, Class<?>> messageClasses = new HashMap<>();
}
