package com.lb.aiagent.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Jackson 字符串与对象转换工具类
 */
public class JacksonUtil {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    static {
        // 注册 Java 8 时间模块
        objectMapper.registerModule(new JavaTimeModule());
        // 禁用日期转时间戳
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 美化输出（可选，根据需求开启）
        // objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }
    
    /**
     * 对象转字符串
     * @param obj 对象
     * @return JSON 字符串
     */
    public static String toJsonString(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("对象转JSON字符串失败", e);
        }
    }
    
    /**
     * 字符串转对象
     * @param json JSON 字符串
     * @param clazz 目标类
     * @param <T> 泛型类型
     * @return 对象实例
     */
    public static <T> T toObject(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new RuntimeException("JSON字符串转对象失败", e);
        }
    }
    
    /**
     * 字符串转集合
     * @param json JSON 字符串
     * @param clazz 集合元素类
     * @param <T> 泛型类型
     * @return 集合实例
     */
    public static <T> List<T> toList(String json, Class<T> clazz) {
        try {
            JavaType javaType = objectMapper.getTypeFactory().constructCollectionType(List.class, clazz);
            return objectMapper.readValue(json, javaType);
        } catch (IOException e) {
            throw new RuntimeException("JSON字符串转集合失败", e);
        }
    }
    
    /**
     * 字符串转Map
     * @param json JSON 字符串
     * @param keyClass Map的key类型
     * @param valueClass Map的value类型
     * @param <K> key泛型类型
     * @param <V> value泛型类型
     * @return Map实例
     */
    public static <K, V> Map<K, V> toMap(String json, Class<K> keyClass, Class<V> valueClass) {
        try {
            JavaType javaType = objectMapper.getTypeFactory().constructMapType(Map.class, keyClass, valueClass);
            return objectMapper.readValue(json, javaType);
        } catch (IOException e) {
            throw new RuntimeException("JSON字符串转Map失败", e);
        }
    }
    
    /**
     * 字符串转复杂类型（如List<Map<String, Object>>等）
     * @param json JSON 字符串
     * @param typeReference 类型引用
     * @param <T> 泛型类型
     * @return 复杂类型实例
     */
    public static <T> T toComplexType(String json, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (IOException e) {
            throw new RuntimeException("JSON字符串转复杂类型失败", e);
        }
    }
    
    /**
     * 格式化输出JSON字符串（美化输出）
     * @param obj 对象
     * @return 格式化后的JSON字符串
     */
    public static String toPrettyJsonString(Object obj) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("对象转格式化JSON字符串失败", e);
        }
    }
}