package com.feiye.advance.chatroom.protocol;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.*;

import java.io.*;
import java.lang.reflect.Type;

/**
 * gson throwable序列化存在问题
 * FastJson2反序列化之后interfaceName为null
 */
public interface Serializer {


    // 反序列化方法：对象类型
    <T> T deserialize(Class<T> clazz, byte[] bytes);

    // 序列化方法
    <T> byte[] serialize(T object);

    //枚举类：可以为不同的序列化算法提供统一的接口调用方式
    enum Algorithm implements Serializer {

        Java {
            @Override
            public <T> T deserialize(Class<T> clazz, byte[] bytes) {
                try {
                    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
                    return (T) ois.readObject();
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException("反序列化失败", e);
                }
            }

            @Override
            public <T> byte[] serialize(T object) {
                try {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(bos);
                    oos.writeObject(object);
                    return bos.toByteArray();
                } catch (IOException e) {
                    throw new RuntimeException("序列化失败", e);
                }
            }
        },
        /*Gson {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Class.class, new ClassCodec())
                    .create();

            @Override
            public <T> T deserialize(Class<T> clazz, byte[] bytes) {
                String json = new String(bytes, StandardCharsets.UTF_8);
                return gson.fromJson(json, clazz);
            }

            @Override
            public <T> byte[] serialize(T object) {
                *//*String json = new Gson().toJson(object);*//*
                String json = gson.toJson(object);
                return json.getBytes(StandardCharsets.UTF_8);
            }
        },*/
        /*FastJson {
            @Override
            public <T> T deserialize(Class<T> clazz, byte[] bytes) {
                System.out.println("FastJson 反序列化开始");
                return JSON.parseObject(bytes, clazz);
            }
            @Override
            public <T> byte[] serialize(T object) {
                return JSON.toJSONBytes(object);
            }
        },*/
        Jackson {
            private ObjectMapper mapper = new ObjectMapper();

            @Override
            public <T> T deserialize(Class<T> clazz, byte[] bytes) {
                try {
                    System.out.println("Jackson 反序列化开始");
                    return mapper.readValue(bytes, clazz);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public <T> byte[] serialize(T object) {
                try {
                    return mapper.writeValueAsBytes(object);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * 自定义类转换器:java class和json字符串对应
     */
    class ClassCodec implements JsonSerializer<Class<?>>, JsonDeserializer<Class<?>> {

        @Override
        public Class<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            try {
                String str = json.getAsString();
                // json->class
                return Class.forName(str);
            } catch (ClassNotFoundException e) {
                throw new JsonParseException(e);
            }
        }

        @Override             //   String.class
        public JsonElement serialize(Class<?> src, Type typeOfSrc, JsonSerializationContext context) {
            //class->json: 字符串在gson中属于primitive
            return new JsonPrimitive(src.getName());
        }
    }
}
