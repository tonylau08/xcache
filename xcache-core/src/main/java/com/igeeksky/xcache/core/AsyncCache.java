package com.igeeksky.xcache.core;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * @author Patrick.Lau
 * @date 2021-06-03
 */
public interface AsyncCache<K, V> {

    CompletableFuture<CacheValue<V>> asyncGet(K key);

    CompletableFuture<Map<K, CacheValue<V>>> asyncGetAll(Set<? extends K> keys);

    CompletableFuture<Void> asyncPutAll(CompletableFuture<Map<? extends K, ? extends V>> keyValuesFuture);

    CompletableFuture<Void> asyncPut(K key, CompletableFuture<V> valueFuture);

    CompletableFuture<Void> asyncRemove(K key);

}
