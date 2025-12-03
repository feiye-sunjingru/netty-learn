package com.feiye.source;

import com.feiye.advance.chatroom.protocol.Serializer;
import com.google.gson.*;

public class TestGson {
    public static void main(String[] args) {
//        Gson gson = new Gson();
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Class.class, new Serializer.ClassCodec())
                .create();
        System.out.println(gson.toJson(String.class));
    }
}
