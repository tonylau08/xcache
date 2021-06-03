package com.igeeksky.xcache.spring;

import org.springframework.cache.Cache;

import java.util.concurrent.Callable;

/**
 * @author Patrick.Lau
 * @date 2021-06-03
 */
public class DeFaultXcache implements Cache {

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Object getNativeCache() {
        return null;
    }

    @Override
    public ValueWrapper get(Object key) {
        return null;
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        return null;
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
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
