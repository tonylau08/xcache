package com.igeeksky.xcache.spring;

import com.igeeksky.xcache.core.Xcache;
import org.jetbrains.annotations.Nullable;
import org.springframework.cache.Cache;

import java.util.concurrent.Callable;

/**
 * @author Patrick.Lau
 * @date 2021-06-03
 */
public class XcacheSpring implements Cache {

    private final String name;
    private final Xcache<Object, Object> xcache;

    public XcacheSpring(String name, Xcache<Object, Object> xcache) {
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
        com.igeeksky.xcache.core.ValueWrapper<Object> wrapper = xcache.sync().get(key);
        return toValueWrapper(wrapper);
    }

    @Nullable
    private ValueWrapper toValueWrapper(com.igeeksky.xcache.core.ValueWrapper<Object> wrapper) {
        if (null != wrapper) {
            return () -> wrapper.getValue();
        }
        return null;
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        com.igeeksky.xcache.core.ValueWrapper<Object> wrapper = xcache.sync().get(key);
        return null == wrapper ? null : (T) wrapper.getValue();
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        com.igeeksky.xcache.core.ValueWrapper<Object> wrapper = xcache.sync().get(key);
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
        xcache.sync().put(key, value);
    }

    @Override
    public void evict(Object key) {
        xcache.sync().remove(key);
    }

    @Override
    public void clear() {
        xcache.sync().clear();
    }

}
