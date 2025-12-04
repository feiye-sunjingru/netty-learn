package com.feiye.advance.chatroom.utils;

/**
 * RPC调用异常类
 * 用于封装RPC远程调用过程中发生的各种异常情况
 */
public class RpcException extends Exception {
    public RpcException(String message) {
        super(message);
    }

    // 重写此方法，避免填充栈跟踪信息
    @Override
    public synchronized Throwable fillInStackTrace() {
        return null;
    }
}
