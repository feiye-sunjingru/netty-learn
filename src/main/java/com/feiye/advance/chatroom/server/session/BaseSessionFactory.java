package com.feiye.advance.chatroom.server.session;

import lombok.Getter;

public abstract class BaseSessionFactory {

    @Getter
    private static final Session session = new SessionMemoryImpl();

}
