package com.feiye.advance.chatroom.config;

import com.feiye.advance.chatroom.protocol.Serializer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public abstract class Config {
    static Properties properties;

    static {
        try (InputStream in = Config.class.getResourceAsStream("/application.properties")) {
            properties = new Properties();
            properties.load(in);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static int getServerPort() {
        String value = properties.getProperty("server.port");
        if (value == null) {
            return 8080;
        } else {
            return Integer.parseInt(value);
        }
    }

    /**
     * 获取序列化算法: 把字符串映射成对应的对象
     *
     * @return 序列化算法
     */
    public static Serializer.Algorithm getSerializerAlgorithm() {
        String value = properties.getProperty("serializer.algorithm");
        if (value == null) {
            return Serializer.Algorithm.Java;
        } else {
            //valueOf能讲字符串转成枚举对象
            return Serializer.Algorithm.valueOf(value);
        }
    }
}