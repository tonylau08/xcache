package com.igeeksky.xcache.core;

import com.igeeksky.xcache.core.config.CacheConfig;
import com.igeeksky.xcache.core.extend.LockSupport;
import com.igeeksky.xcache.core.refresh.RefreshCache;
import com.igeeksky.xcache.core.refresh.RefreshCacheEvent;
import com.igeeksky.xcache.core.statistic.CacheStatisticsHolder;
import com.igeeksky.xcache.core.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * @author Patrick.Lau
 * @date 2020-12-11
 */
public class DefaultCache<K, V> implements Cache<K, V>, RefreshCache {


    private volatile SyncCache<K, V> syncCache;

    private volatile AsyncCache<K, V> asyncCache;

    private final String name;
    private final CacheLevel cacheLevel;
    private final String namespace;

    protected final Function<K, V> loader;

    protected BiPredicate<String, K> containsPredicate = new BiPredicate<String, K>() {
        @Override
        public boolean test(String name, K key) {
            return true;
        }
    };

    protected LockSupport<K> lockSupport;

    protected CacheStore<K, V> cacheStore;

    protected boolean allowRecord = false;

    protected boolean allowNullValue;

    protected CacheStatisticsHolder statisticsHolder;

    private final Object lock = new Object();

    public DefaultCache(CacheConfig<K, V> cacheConfig) {
        this.name = cacheConfig.getName();
        this.cacheLevel = cacheConfig.getCacheLevel();
        this.namespace = cacheConfig.getNamespace();
        this.cacheStore = cacheConfig.getCacheStore();
        this.loader = cacheConfig.getLoader();
        this.statisticsHolder = cacheConfig.getCacheStatisticsHolder();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public CacheLevel getCacheLevel() {
        return cacheLevel;
    }

    @Override
    public Mono<CacheValue<V>> get(K key) {
        if (null == key) {
            return Mono.error(new NullPointerException("key must not be null."));
        }
        return doGet(key).doOnNext(cacheValue -> {
            if (allowRecord) {
                statisticsHolder.recordGets(cacheValue);
            }
        });
    }

    @Override
    public Mono<CacheValue<V>> get(K key, Callable<V> valueLoader) {
        if (null == key) {
            return Mono.error(new NullPointerException("key must not be null."));
        }
        Lock keyLock = lockSupport.getLock(key);
        return get(key)
                .filter(cacheValue -> (null == cacheValue) && (containsPredicate.test(name, key)))
                .doOnNext(cacheValue -> keyLock.lock())
                .flatMap(cacheValue -> doGet(key))
                .filter(cacheValue -> cacheValue == null)
                .flatMap(cacheValue -> Mono.fromCallable(valueLoader))
                .flatMap(value -> doPut(key, value))
                .doFinally(s -> keyLock.unlock());
    }

    protected Mono<CacheValue<V>> doGet(K key) {
        return cacheStore.get(key);
    }

    @Override
    public Flux<KeyValue<K, CacheValue<V>>> getAll(Set<? extends K> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            return Flux.empty();
        }
        return cacheStore.getAll(keys);
    }

    @Override
    public Mono<Void> put(K key, Mono<V> value) {
        if (null == key) {
            return Mono.error(new NullPointerException("key must not be null."));
        }
        return value.flatMap(v -> doPut(key, v)).then();
    }

    @Override
    public Mono<Void> putAll(Mono<Map<? extends K, ? extends V>> keyValues) {
        keyValues.filter(CollectionUtils::isNotEmpty).map(map -> {
            Iterator<? extends Map.Entry<? extends K, ? extends V>> it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<? extends K, ? extends V> entry = it.next();
                K key = entry.getKey();
                if (null == key) {
                    it.remove();
                    continue;
                }
                V value = entry.getValue();
                if (null == value && !allowNullValue) {
                    it.remove();
                }
            }
            return map;
        });
        return cacheStore.putAll(keyValues);
    }

    @Override
    public Mono<Void> remove(K key) {
        if (null == key) {
            return Mono.error(new NullPointerException("key must not be null."));
        }
        return cacheStore.remove(key).then();
    }

    @Override
    public void clear() {
        cacheStore.clear();
    }

    @Override
    public void onEvent(RefreshCacheEvent event) {

    }

    @Override
    public SyncCache<K, V> sync() {
        if (null == syncCache) {
            synchronized (lock) {
                if (null == syncCache) {
                    this.syncCache = new SyncCache.SyncCacheView<>(this);
                }
            }
        }
        return syncCache;
    }

    @Override
    public AsyncCache<K, V> async() {
        if (null == asyncCache) {
            synchronized (lock) {
                if (null == asyncCache) {
                    this.asyncCache = new AsyncCache.AsyncCacheView<>(this);
                }
            }
        }
        return asyncCache;
    }

    protected Mono<CacheValue<V>> doPut(K key, V value) {
        if (null != value || allowNullValue) {
            return cacheStore.put(key, value);
        }
        return Mono.empty();
    }

}
