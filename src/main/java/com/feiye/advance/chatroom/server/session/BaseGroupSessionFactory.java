package com.feiye.advance.chatroom.server.session;

public abstract class BaseGroupSessionFactory {

    private static GroupSession session = new GroupSessionMemoryImpl();

    public static GroupSession getGroupSession() {
        return session;
    }
}
