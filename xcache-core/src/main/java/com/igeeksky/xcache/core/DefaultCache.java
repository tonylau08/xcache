package com.igeeksky.xcache.core;

import com.igeeksky.xcache.core.extend.LockSupport;
import com.igeeksky.xcache.core.refresh.RefreshCache;
import com.igeeksky.xcache.core.refresh.RefreshEvent;
import com.igeeksky.xcache.core.statistic.CacheStatisticsHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author Patrick.Lau
 * @date 2020-12-11
 */
public class DefaultCache<K, V> implements Cache<K, V>, RefreshCache {

    private static final Logger log = LoggerFactory.getLogger(DefaultCache.class);

    private final String name;

    protected final Function<K, V> loadFunction;

    protected Predicate<K> containsPredicate;

    protected LockSupport<K> lockSupport;

    protected CacheStore<K, V> cacheStore;

    protected boolean allowRecord;

    protected boolean allowNullValue;

    protected CacheStatisticsHolder statisticsHolder;

    private SyncCache<K, V> syncCache;

    private AsyncCache<K, V> asyncCache;

    private final Object lock = new Object();

    public DefaultCache(String name, CacheStore<K, V> cacheStore, Function<K, V> loadFunction, CacheStatisticsHolder statisticsHolder) {
        this.name = name;
        this.cacheStore = cacheStore;
        this.loadFunction = loadFunction;
        this.statisticsHolder = statisticsHolder;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Mono<CacheValue<V>> get(K key) {
        return doGet(key).doOnNext(cacheValue -> {
            if (allowRecord) {
                statisticsHolder.recordGets(cacheValue);
            }
        });
    }

    @Override
    public Mono<CacheValue<V>> get(K key, Callable<V> valueLoader) {
        return get(key).flatMap(cacheValue -> {
            if (null != cacheValue) {
                return Mono.just(cacheValue);
            }
            if (containsPredicate.test(key)) {
                Lock keyLock = lockSupport.getLock(key);
                return Mono.just(keyLock)
                        .doOnNext(lock -> lock.lock())
                        .flatMap(lock -> this.doGet(key).flatMap(cacheValue2 -> {
                                    if (cacheValue2 != null) {
                                        return Mono.just(cacheValue2);
                                    }
                                    return Mono.fromCallable(valueLoader)
                                            .flatMap(v -> doPut(key, Mono.justOrEmpty(v)));
                                })
                        ).doFinally(s -> keyLock.unlock());
            }
            return Mono.empty();
        });
    }

    protected Mono<CacheValue<V>> doGet(K key) {
        return cacheStore.get(key);
    }

    @Override
    public Flux<KeyValue<K, CacheValue<V>>> getAll(Set<? extends K> keys) {
        return cacheStore.getAll(keys);
    }

    @Override
    public Mono<Void> putAll(Mono<Map<? extends K, ? extends V>> keyValues) {
        return cacheStore.putAll(keyValues);
    }

    @Override
    public Mono<Void> put(K key, Mono<V> value) {
        return doPut(key, value).then();
    }

    @Override
    public Mono<Void> remove(K key) {
        return cacheStore.remove(key).then();
    }

    @Override
    public void clear() {
        cacheStore.clear();
    }

    @Override
    public void onEvent(RefreshEvent event) {

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

    protected Mono<CacheValue<V>> doPut(K key, Mono<V> value) {
        // TODO 过期时间随机？
        // TODO 空值缓存，过期时间短
        return value.filter(v -> (v != null || allowNullValue))
                .flatMap(v -> cacheStore.put(key, Mono.justOrEmpty(v)));
    }

}
