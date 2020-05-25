package com.vergilyn.examples.util;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * TODO 2020-05-18 optimize
 * @author vergilyn
 * @date 2020-05-18
 */
public class DefaultObjectMapper {

    private static final ObjectMapper INSTANCE = new ObjectMapper();
    static {
        // 处理特殊字符 ASCII < 32  EX. 制表符、换行符
        INSTANCE.enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature());
    }

    public static ObjectMapper getInstance(){
        return INSTANCE;
    }

    public static <T> T readValue(String json, Class<T> clazz){
        T rs = null;
        try {
            rs = getInstance().readValue(json, clazz);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return rs;
    }

    public static <T> List<T> readValueAsList(String json, Class<T> clazz){
        List<T> rs = null;
        try {
            rs = getInstance().readValue(json, new TypeReference<List<T>>() {});
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return rs;
    }

    public static String writeValueAsString(Object value){
        String rs = null;
        try {
            rs = getInstance().writeValueAsString(value);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return rs;
    }
}
