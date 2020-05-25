package com.vergilyn.examples.util;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;

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

        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));

        javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        INSTANCE.registerModule(javaTimeModule);
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
