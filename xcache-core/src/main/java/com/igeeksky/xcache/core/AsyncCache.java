package com.igeeksky.xcache.core;

import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author Patrick.Lau
 * @date 2021-06-03
 */
public interface AsyncCache<K, V> {

    CompletableFuture<CacheValue<V>> get(K key);

    CompletableFuture<Map<K, CacheValue<V>>> getAll(Set<? extends K> keys);

    CompletableFuture<Void> putAll(CompletableFuture<Map<? extends K, ? extends V>> keyValues);

    CompletableFuture<Void> put(K key, CompletableFuture<V> value);

    CompletableFuture<Void> remove(K key);

    class AsyncCacheView<K, V> implements AsyncCache<K, V> {

        private Cache<K, V> cache;

        public AsyncCacheView(Cache<K, V> cache) {
            this.cache = cache;
        }

        @Override
        public CompletableFuture<CacheValue<V>> get(K key) {
            return cache.get(key).toFuture();
        }

        @Override
        public CompletableFuture<Map<K, CacheValue<V>>> getAll(Set<? extends K> keys) {
            return cache.getAll(keys)
                    .collect(Collectors.toMap(KeyValue::getKey, KeyValue::getValue, (k, v) -> v))
                    .toFuture();
        }

        @Override
        public CompletableFuture<Void> putAll(CompletableFuture<Map<? extends K, ? extends V>> keyValues) {
            return cache.putAll(Mono.fromFuture(keyValues)).toFuture();
        }

        @Override
        public CompletableFuture<Void> put(K key, CompletableFuture<V> value) {
            return cache.put(key, Mono.fromFuture(value)).toFuture();
        }

        @Override
        public CompletableFuture<Void> remove(K key) {
            return cache.remove(key).toFuture();
        }
    }

}
