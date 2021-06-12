package com.igeeksky.xcache.core.statistic;

import com.igeeksky.xcache.core.CacheValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author: Patrick.Lau
 * @date: 2021-06-11
 */
public class CacheStatisticsHolder {

    private final Logger log = LoggerFactory.getLogger(CacheStatisticsHolder.class);

    private final String name;
    private CacheStatisticsProvider provider;
    private CacheStatisticsPublisher publisher;

    private final List<CacheStatistics> statisticsList = new LinkedList<>();
    private AtomicReference<CacheStatistics> reference;

    // TODO 定时切换新的缓存统计对象实例

    public CacheStatisticsHolder(String name, CacheStatisticsProvider provider, CacheStatisticsPublisher publisher) {
        this.name = name;
        this.provider = provider;
        this.publisher = publisher;
    }

    public String getName() {
        return name;
    }

    public void incHits() {
        try {
            reference.get().incHits();
        } catch (Exception e) {
            log.error("cache statistics error.", e);
        }
    }

    public CompletableFuture<Void> asyncIncHits() {
        return CompletableFuture.runAsync(() -> {
            incHits();
        });
    }

    public void incMisses() {
        try {
            reference.get().incMisses();
        } catch (Exception e) {
            log.error("cache statistics error.", e);
        }
    }

    public CompletableFuture<Void> asyncIncMisses() {
        return CompletableFuture.runAsync(() -> {
            incMisses();
        });
    }

    public <V> void recordGets(CacheValue<V> cacheValue) {
        if (null != cacheValue) {
            incHits();
        } else {
            incMisses();
        }
    }

    public <V> CompletableFuture<CacheValue<V>> asyncRecordGets(CompletableFuture<CacheValue<V>> future) {
        return future.thenApply(vCacheValue -> {
            if (null != vCacheValue) {
                asyncIncHits();
            } else {
                asyncIncMisses();
            }
            return vCacheValue;
        });
    }

    public <K, V> void recordGetAll(Map<K, V> keyValues) {
        // TODO 统计
    }

    public <K, V> Map<K, CompletableFuture<CacheValue<V>>> asyncRecordGetAll(CompletableFuture<Map<K, CacheValue<V>>> futureMap) {

        return null;
    }
}
