package com.lb.aiagent.utils;

import cn.hutool.core.util.StrUtil;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Map工具类
 */
public class MapUtil {

    /**
     * 检查Map是否为空或null
     *
     * @param map 待检查的Map
     * @return 如果为空或null则返回true，否则返回false
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /**
     * 检查Map是否不为空
     *
     * @param map 待检查的Map
     * @return 如果不为空则返回true，否则返回false
     */
    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    /**
     * 安全地从Map中获取值，如果Map为null或键不存在则返回默认值
     *
     * @param map          Map对象
     * @param key          键
     * @param defaultValue 默认值
     * @param <K>          键类型
     * @param <V>          值类型
     * @return 值或默认值
     */
    public static <K, V> V getOrDefault(Map<K, V> map, K key, V defaultValue) {
        if (isEmpty(map)) {
            return defaultValue;
        }
        return map.getOrDefault(key, defaultValue);
    }

    /**
     * 将两个Map合并，第二个Map的值会覆盖第一个Map中相同键的值
     *
     * @param map1 第一个Map
     * @param map2 第二个Map
     * @param <K>  键类型
     * @param <V>  值类型
     * @return 合并后的新Map
     */
    public static <K, V> Map<K, V> merge(Map<K, V> map1, Map<K, V> map2) {
        Map<K, V> result = new HashMap<>();
        if (isNotEmpty(map1)) {
            result.putAll(map1);
        }
        if (isNotEmpty(map2)) {
            result.putAll(map2);
        }
        return result;
    }

    /**
     * 从集合创建Map，使用指定的键函数和值函数
     *
     * @param collection  集合
     * @param keyFunction 键映射函数
     * @param valueFunction 值映射函数
     * @param <T>         集合元素类型
     * @param <K>         键类型
     * @param <V>         值类型
     * @return 创建的Map
     */
    public static <T, K, V> Map<K, V> toMap(Collection<T> collection,
                                          Function<? super T, ? extends K> keyFunction,
                                          Function<? super T, ? extends V> valueFunction) {
        if (collection == null || collection.isEmpty()) {
            return Collections.emptyMap();
        }
        return collection.stream().collect(Collectors.toMap(keyFunction, valueFunction));
    }

    /**
     * 反转Map的键和值
     *
     * @param map 原始Map
     * @param <K> 键类型
     * @param <V> 值类型
     * @return 反转后的Map
     */
    public static <K, V> Map<V, K> invert(Map<K, V> map) {
        if (isEmpty(map)) {
            return Collections.emptyMap();
        }
        return map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    }

    /**
     * 获取Map的不可变副本
     *
     * @param map 原始Map
     * @param <K> 键类型
     * @param <V> 值类型
     * @return 不可变Map副本
     */
    public static <K, V> Map<K, V> unmodifiableMap(Map<K, V> map) {
        if (isEmpty(map)) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(map);
    }

    /**
     * 从Map中移除指定的键值对
     *
     * @param map Map对象
     * @param key 键
     * @param value 值
     * @param <K> 键类型
     * @param <V> 值类型
     * @return 如果成功移除则返回true，否则返回false
     */
    public static <K, V> boolean remove(Map<K, V> map, K key, V value) {
        if (isEmpty(map)) {
            return false;
        }
        return map.remove(key, value);
    }

    /**
     * 获取Map中键的集合
     *
     * @param map Map对象
     * @param <K> 键类型
     * @param <V> 值类型
     * @return 键集合
     */
    public static <K, V> Collection<K> keys(Map<K, V> map) {
        if (isEmpty(map)) {
            return Collections.emptyList();
        }
        return map.keySet();
    }

    /**
     * 获取Map中值的集合
     *
     * @param map Map对象
     * @param <K> 键类型
     * @param <V> 值类型
     * @return 值集合
     */
    public static <K, V> Collection<V> values(Map<K, V> map) {
        if (isEmpty(map)) {
            return Collections.emptyList();
        }
        return map.values();
    }

    /**
     * 获取指定类型的值，支持默认值
     *
     * @param map Map对象
     * @param key 键
     * @param targetType 目标类型Class
     * @param defaultValue 默认值
     * @param <T> 泛型类型
     * @return 指定类型的值或默认值
     */
    public static <T> T getOrDefault(Map<?, ?> map, Object key, Class<T> targetType, T defaultValue) {
        if (map == null) {
            return defaultValue;
        }

        Object value = map.get(key);
        if (value == null) {
            return defaultValue;
        }

        return convertValue(value, targetType);
    }

    /**
     * 获取指定类型的值，不支持默认值
     *
     * @param map Map对象
     * @param key 键
     * @param targetType 目标类型Class
     * @param <T> 泛型类型
     * @return 指定类型的值，如果转换失败返回null
     */
    public static <T> T get(Map<?, ?> map, Object key, Class<T> targetType) {
        return getOrDefault(map, key, targetType, null);
    }

    /**
     * 类型转换方法
     *
     * @param value 原始值
     * @param targetType 目标类型
     * @param <T> 泛型类型
     * @return 转换后的值
     */
    @SuppressWarnings("unchecked")
    private static <T> T convertValue(Object value, Class<T> targetType) {
        if (value == null) {
            return null;
        }

        if (targetType.isInstance(value)) {
            return (T) value;
        }

        // 字符串类型转换
        if (value instanceof String) {
            String strValue = (String) value;
            if (StrUtil.isBlank(strValue)) {
                return null;
            }

            if (targetType == String.class) {
                return (T) strValue;
            } else if (targetType == Integer.class || targetType == int.class) {
                return (T) Integer.valueOf(strValue);
            } else if (targetType == Long.class || targetType == long.class) {
                return (T) Long.valueOf(strValue);
            } else if (targetType == Double.class || targetType == double.class) {
                return (T) Double.valueOf(strValue);
            } else if (targetType == Boolean.class || targetType == boolean.class) {
                return (T) Boolean.valueOf(strValue);
            }
        }

        // 数字类型转换
        if (value instanceof Number) {
            Number numValue = (Number) value;
            if (targetType == Integer.class || targetType == int.class) {
                return (T) Integer.valueOf(numValue.intValue());
            } else if (targetType == Long.class || targetType == long.class) {
                return (T) Long.valueOf(numValue.longValue());
            } else if (targetType == Double.class || targetType == double.class) {
                return (T) Double.valueOf(numValue.doubleValue());
            } else if (targetType == String.class) {
                return (T) numValue.toString();
            }
        }

        // 布尔类型转换
        if (value instanceof Boolean && targetType == String.class) {
            return (T) value.toString();
        }

        return null;
    }

    public static <T> List<T> getList(Map map, String key, Class<T> clazz) {
        Object o = map.get(key);
        if (o == null) {
            return List.of();
        }
        return JacksonUtil.toList(JacksonUtil.toJsonString(o), clazz);
    }
}