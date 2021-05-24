package com.igeeksky.xcache.core;

import java.util.concurrent.CompletableFuture;

/**
 * @author Patrick.Lau
 * @date 2021-05-24
 */
public interface AsyncCache<K, V> {

    CompletableFuture<CacheStore.ValueWrapper<V>> asyncGet(K key);

}
