package com.igeeksky.xcache.spring;

import com.igeeksky.xcache.core.CacheValue;
import com.igeeksky.xcache.core.Cache;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Callable;

/**
 * @author Patrick.Lau
 * @date 2021-06-03
 */
public class XcacheSpring implements org.springframework.cache.Cache {

    private final String name;
    private final Cache<Object, Object> xcache;

    public XcacheSpring(String name, Cache<Object, Object> xcache) {
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
        CacheValue<Object> cacheValue = xcache.sync().get(key);
        return toValueWrapper(cacheValue);
    }

    @Nullable
    private ValueWrapper toValueWrapper(CacheValue<Object> wrapper) {
        return null == wrapper ? null : (wrapper::getValue);
    }


    @Override
    public <T> T get(Object key, Class<T> type) {
        CacheValue<Object> cacheValue = xcache.sync().get(key);
        return fromStoreValue(cacheValue);
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        CacheValue<Object> cacheValue = xcache.sync().get(key, () -> valueLoader.call());
        return fromStoreValue(cacheValue);
    }

    @SuppressWarnings("unchecked")
    private <T> T fromStoreValue(CacheValue<Object> cacheValue) {
        return null == cacheValue ? null : (T) cacheValue.getValue();
    }

    @Override
    public void put(Object key, Object value) {
        xcache.sync().put(key, value);
    }

    @Override
    public void evict(Object key) {
        xcache.remove(key);
    }

    @Override
    public void clear() {
        xcache.clear();
    }

}
