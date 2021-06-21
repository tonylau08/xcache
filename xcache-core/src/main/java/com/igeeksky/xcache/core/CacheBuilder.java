package com.igeeksky.xcache.core;

public interface CacheBuilder {

    Cache<Object, Object> build(String name);

    <K, V> Cache<K, V> build(String name, Class<K> keyClazz, Class<V> valueClazz);

}
