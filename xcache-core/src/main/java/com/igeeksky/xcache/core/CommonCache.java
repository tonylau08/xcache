package com.igeeksky.xcache.core;

import com.igeeksky.xcache.core.extend.LockSupport;
import com.igeeksky.xcache.core.refresh.RefreshCache;
import com.igeeksky.xcache.core.refresh.RefreshEvent;
import com.igeeksky.xcache.core.statistic.CacheStatisticsHolder;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.Lock;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author Patrick.Lau
 * @date 2020-12-11
 */
public class CommonCache<K, V> implements XCache<K, V>, RefreshCache {

    private static final Logger log = LoggerFactory.getLogger(CommonCache.class);

    private final String name;

    protected final Function<K, V> loadFunction;

    protected Predicate<K> containsPredicate;

    protected LockSupport<K> lockSupport;

    protected CacheStore<K, V> cacheStore;

    protected boolean allowRecord;

    protected boolean allowNullValue;

    protected CacheStatisticsHolder statisticsHolder;

    public CommonCache(String name, CacheStore<K, V> cacheStore, Function<K, V> loadFunction, CacheStatisticsHolder statisticsHolder) {
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
    public CacheValue<V> get(K key) {
        CacheValue<V> cacheValue = doGet(key);
        if (allowRecord) {
            statisticsHolder.recordGets(cacheValue);
        }
        return cacheValue;
    }

    protected CacheValue<V> doGet(K key) {
        return cacheStore.get(key);
    }

    @Override
    public CacheValue<V> get(K key, Callable<V> valueLoader) {
        CacheValue<V> wrapper = get(key);
        if (null != wrapper) {
            return wrapper;
        }

        if (containsPredicate.test(key)) {
            Lock lock = lockSupport.getLock(key);
            lock.lock();
            try {
                wrapper = doGet(key);
                if (wrapper != null) {
                    return wrapper;
                }
                V value = valueLoader.call();
                // TODO 数据统计
                return doPut(key, value);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException(e);
            } finally {
                lock.unlock();
            }
        }
        return null;
    }

    @Override
    public Map<K, CacheValue<V>> getAll(Set<? extends K> keys) {
        Map<K, CacheValue<V>> keyValues = doGetAll(keys);
        if (allowRecord) {
            statisticsHolder.recordGetAll(keyValues);
        }
        return keyValues;
    }

    private Map<K, CacheValue<V>> doGetAll(Set<? extends K> keys) {
        return cacheStore.getAll(keys);
    }

    @Override
    public void put(K key, V value) {
        doPut(key, value);
    }

    protected CacheValue<V> doPut(K key, V value) {
        // TODO 过期时间随机？
        // TODO 空值缓存，过期时间短
        // TODO 是否允许空值
        if (null != value || allowNullValue) {
            return cacheStore.put(key, value);
        }
        return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> keyValues) {
        cacheStore.putAll(keyValues);
    }

    @Override
    public void remove(K key) {
        cacheStore.remove(key);
    }

    @Override
    public void clear() {
        cacheStore.clear();
    }

    @Override
    public void onEvent(RefreshEvent event) {

    }

    @Override
    public CompletableFuture<CacheValue<V>> asyncGet(K key) {
        CompletableFuture<CacheValue<V>> future = cacheStore.asyncGet(key);
        return allowRecord ? statisticsHolder.asyncRecordGets(future) : future;
    }

    public CompletableFuture<CacheValue<V>> asyncGet(K key, Callable<V> valueLoader) {
        CompletableFuture<CacheValue<V>> future = cacheStore.asyncGet(key);
        future.thenCompose(vCacheValue -> {
            if (null == vCacheValue) {
                if (containsPredicate.test(key)) {
                    return asyncGet(key)
                            .thenCompose(reGetFutureValue -> extracted(key, valueLoader))
                            .thenCompose(v -> asyncGet(key));
                }
            }
            return future;
        });

        return allowRecord ? statisticsHolder.asyncRecordGets(future) : future;
    }

    private CompletableFuture<Void> extracted(K key, Callable<V> valueLoader) {
        return asyncPut(key, getValueFuture(key, valueLoader));
    }

    @NotNull
    private CompletableFuture<V> getValueFuture(K key, Callable<V> valueLoader) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Lock lock = lockSupport.getLock(key);
                lock.lock();
                try {
                    return valueLoader.call();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e);
                } finally {
                    lock.unlock();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    @Override
    public Map<K, CompletableFuture<CacheValue<V>>> asyncGetAll(Set<? extends K> keys) {
        Map<K, CompletableFuture<CacheValue<V>>> futureMap = cacheStore.asyncGetAll(keys);
        if (allowRecord) {
            return statisticsHolder.asyncRecordGetAll(futureMap);
        }
        return futureMap;
    }

    @Override
    public CompletableFuture<Void> asyncPutAll(CompletableFuture<Map<? extends K, ? extends V>> keyValues) {
        return cacheStore.asyncPutAll(keyValues);
    }

    @Override
    public CompletableFuture<Void> asyncPut(K key, CompletableFuture<V> valueFuture) {
        return valueFuture.thenCompose(value -> {
            if (null != value || allowNullValue) {
                return cacheStore.asyncPut(key, CompletableFuture.completedFuture(value));
            }
            return null;
        });
    }

    @Override
    public CompletableFuture<Void> asyncRemove(K key) {
        return cacheStore.asyncRemove(key);
    }

    @Override
    public Mono<CacheValue<V>> reactiveGet(K key) {
        return cacheStore.reactiveGet(key);
    }

    @Override
    public Flux<KeyValue<K, CacheValue<V>>> reactiveGetAll(Set<? extends K> keys) {
        return cacheStore.reactiveGetAll(keys);
    }

    @Override
    public Mono<Void> reactivePutAll(Mono<Map<? extends K, ? extends V>> keyValues) {
        return cacheStore.reactivePutAll(keyValues);
    }

    @Override
    public Mono<Void> reactivePut(K key, Mono<V> value) {
        return cacheStore.reactivePut(key, value);
    }

    @Override
    public Mono<Void> reactiveRemove(K key) {
        return cacheStore.reactiveRemove(key);
    }

}
