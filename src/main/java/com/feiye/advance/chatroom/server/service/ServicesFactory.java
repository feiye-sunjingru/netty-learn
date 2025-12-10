package com.feiye.advance.chatroom.server.service;

import com.feiye.advance.chatroom.config.Config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 属性名以 Service 结尾的类：接口=实例
 * 适合运行时动态指定接口名的场景（如接口名存在配置文件中），需传入接口的全限定名（包名 + 接口名）
 */
public class ServicesFactory {

    static Properties properties;
    static Map<Class<?>, Object> map = new ConcurrentHashMap<>();

    static {
        try (InputStream in = Config.class.getResourceAsStream("/application.properties")) {
            properties = new Properties();
            properties.load(in);

            Set<String> names = properties.stringPropertyNames();
            for (String name : names) {
                if (name.endsWith("Service")) {
                    //1.传入接口全限定名，获取接口的Class对象
                    Class<?> interfaceClass = Class.forName(name);
                    //2.传入接口全限定名，获取接口实现类的Class对象
                    Class<?> instanceClass = Class.forName(properties.getProperty(name));
                    map.put(interfaceClass, instanceClass.newInstance());
                }
            }
        } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static <T> T getService(Class<T> interfaceClass) {
        return (T) map.get(interfaceClass);
    }
}
