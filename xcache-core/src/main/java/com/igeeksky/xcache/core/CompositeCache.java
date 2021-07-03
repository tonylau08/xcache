package com.igeeksky.xcache.core;

import com.igeeksky.xcache.core.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.Callable;

/**
 * @author Patrick.Lau
 * @date 2021-06-03
 */
public class CompositeCache<K, V> extends AbstractCache<K, V> {

    private final Cache<K, V> firstCache;
    private final Cache<K, V> secondCache;
    private final Cache<K, V> thirdCache;

    public CompositeCache(String name, Cache<K, V> firstCache, Cache<K, V> secondCache) {
        super(name);
        this.firstCache = firstCache;
        this.secondCache = secondCache;
        this.thirdCache = null;
    }

    @Override
    public Mono<CacheValue<V>> get(K key) {
        if (null == key) {
            return Mono.error(new NullPointerException("key must not be null."));
        }
        return firstCache.get(key).switchIfEmpty(
                secondCache.get(key)
                        .doOnNext(cacheValue -> firstCache.put(key, Mono.justOrEmpty(cacheValue.getValue())))
        );
    }

    @Override
    public Mono<CacheValue<V>> get(K key, Callable<V> loader) {
        if (null == key) {
            return Mono.error(new NullPointerException("key must not be null."));
        }
        return firstCache.get(key).switchIfEmpty(
                secondCache.get(key, loader)
                        .filter(Objects::nonNull)
                        .doOnNext(cacheValue -> firstCache.put(key, Mono.justOrEmpty(cacheValue.getValue())))
        );
    }

    @Override
    public Flux<KeyValue<K, CacheValue<V>>> getAll(Set<? extends K> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            return Flux.empty();
        }

        Set<K> keySet = new HashSet<>(keys);
        return firstCache.getAll(keySet)
                .doOnNext(kv -> keySet.remove(kv.getKey()))
                .collect(() -> new ArrayList<KeyValue<K, CacheValue<V>>>(keySet.size()), ArrayList::add)
                .flatMapMany(
                        firstList -> Flux.fromIterable(firstList)
                                .concatWith(
                                        secondCache.getAll(keySet)
                                                .doOnNext(kv -> {
                                                    CacheValue<V> cacheValue = kv.getValue();
                                                    if (null != cacheValue) {
                                                        firstCache.put(kv.getKey(), Mono.justOrEmpty(cacheValue.getValue()));
                                                    }
                                                })
                                ));
    }

    @Override
    public Mono<Void> put(K key, Mono<V> value) {
        if (null == key) {
            return Mono.error(new NullPointerException("key must not be null."));
        }
        return value.flatMap(v -> {
            Mono<Void> second = secondCache.put(key, Mono.justOrEmpty(v));
            Mono<Void> first = firstCache.put(key, Mono.justOrEmpty(v));
            return second.mergeWith(first).then();
        });
    }

    @Override
    public Mono<Void> putAll(Mono<Map<? extends K, ? extends V>> keyValues) {
        return keyValues.filter(CollectionUtils::isNotEmpty)
                .flatMap(kvs -> {
                    Mono<Void> second = secondCache.putAll(Mono.just(kvs));
                    Mono<Void> first = firstCache.putAll(Mono.just(kvs));
                    return second.mergeWith(first).then();
                });
    }

    @Override
    public Mono<Void> remove(K key) {
        if (null == key) {
            return Mono.error(new NullPointerException("key must not be null."));
        }
        return secondCache.remove(key).mergeWith(v -> firstCache.remove(key)).then();
    }

    @Override
    public void clear() {
        firstCache.clear();
        secondCache.clear();
    }

    @Override
    public CacheLevel getCacheLevel() {
        return null;
    }
}
