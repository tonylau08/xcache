package com.igeeksky.xcache.core.extend;

/**
 * @author Patrick.Lau
 * @date 2021-06-21
 */
@FunctionalInterface
public interface SerializerMapper {

    /**
     * 接受对象的Class type，返回序列化器
     * @param clazz 接受类型
     * @param <T> 对象类型
     * @return Serializer<T> 序列化类
     */
    <T> Serializer<T> apply(Class<T> clazz);

}
