package com.igeeksky.xcache.core;

import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * @author Patrick.Lau
 * @date 2020-12-11
 */
public interface SyncCache<K, V> {

    CacheValue<V> get(K key);

    CacheValue<V> get(K key, Callable<V> loader);

    Map<K, CacheValue<V>> getAll(Set<? extends K> keys);

    void put(K key, V value);

    void putAll(Map<? extends K, ? extends V> map);

    void remove(K key);

    class SyncCacheView<K, V> implements SyncCache<K, V> {

        private Cache<K, V> cache;

        public SyncCacheView(Cache<K, V> cache) {
            this.cache = cache;
        }

        @Override
        public CacheValue<V> get(K key) {
            return cache.get(key).block();
        }

        @Override
        public CacheValue<V> get(K key, Callable<V> loader) {
            return cache.get(key, loader).block();
        }

        @Override
        public Map<K, CacheValue<V>> getAll(Set<? extends K> keys) {
            return cache.getAll(keys)
                    .collect(Collectors.toMap(KeyValue::getKey, KeyValue::getValue, (a, b) -> b))
                    .block();
        }

        @Override
        public void put(K key, V value) {
            cache.put(key, Mono.justOrEmpty(value)).block();
        }

        @Override
        public void putAll(Map<? extends K, ? extends V> map) {
            cache.putAll(Mono.justOrEmpty(map)).block();
        }

        @Override
        public void remove(K key) {
            cache.remove(key).block();
        }
    }

}
