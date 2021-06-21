package com.igeeksky.xcache.core.extend;

/**
 * @author: Patrick.Lau
 * @date: 2021-06-21
 */
@FunctionalInterface
public interface SerializerFunction {

    <T> Serializer<T> apply(java.lang.Class<T> clazz);

}
