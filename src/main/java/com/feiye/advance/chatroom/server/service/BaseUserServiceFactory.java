package com.feiye.advance.chatroom.server.service;

/**
 * 工厂类，获取一个UserService实例
 */
public abstract class BaseUserServiceFactory {

    private static UserService userService = new UserServiceMemoryImpl();

    public static UserService getUserService() {
        return userService;
    }
}
