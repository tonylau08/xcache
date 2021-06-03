package com.igeeksky.xcache.core;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionStage;

/**
 * @author Patrick.Lau
 * @date 2021-06-03
 */
public interface AsyncCache<K, V> {

    CompletionStage<ValueWrapper<V>> get(K key);

    CompletionStage<Map<K, V>> getAll(Set<? extends K> keys);

    CompletionStage<Void> putAll(java.util.Map<? extends K, ? extends V> map);

    CompletionStage<Void> put(K key, V value);

    CompletionStage<ValueWrapper<V>> putIfAbsent(K key, V value);

    CompletionStage<Void> remove(K key);

    CompletionStage<Void> clear();

}
