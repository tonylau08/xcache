package com.igeeksky.xcache.spring;

import com.igeeksky.xcache.core.Xcache;
import org.jetbrains.annotations.Nullable;
import org.springframework.cache.Cache;

import java.util.concurrent.Callable;

/**
 * @author Patrick.Lau
 * @date 2021-06-03
 */
public class XcacheSpring<V> implements Cache {

    private final String name;
    private final Xcache<Object, V> xcache;

    public XcacheSpring(String name, Xcache<Object, V> xcache) {
        this.name = name;
        this.xcache = xcache;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getNativeCache() {
        return xcache;
    }

    @Override
    public ValueWrapper get(Object key) {
        com.igeeksky.xcache.core.ValueWrapper<V> wrapper = xcache.sync().get(key);
        return toValueWrapper(wrapper);
    }

    @Nullable
    private ValueWrapper toValueWrapper(com.igeeksky.xcache.core.ValueWrapper<V> wrapper) {
        if (null != wrapper) {
            return () -> wrapper.getValue();
        }
        return null;
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        com.igeeksky.xcache.core.ValueWrapper<V> wrapper = xcache.sync().get(key);
        return null == wrapper ? null : (T) wrapper.getValue();
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        com.igeeksky.xcache.core.ValueWrapper<V> wrapper = xcache.sync().get(key);
        if (null != wrapper) {
            return (T) wrapper.getValue();
        }
        try {
            return valueLoader.call();
        } catch (Exception e) {
            //TODO 修改为重试错误
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void put(Object key, Object value) {

    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        return Cache.super.putIfAbsent(key, value);
    }

    @Override
    public void evict(Object key) {

    }

    @Override
    public boolean evictIfPresent(Object key) {
        return Cache.super.evictIfPresent(key);
    }

    @Override
    public void clear() {

    }

    @Override
    public boolean invalidate() {
        return Cache.super.invalidate();
    }
}
