package com.igeeksky.xcache.core.config;

import com.igeeksky.xcache.core.CacheLevel;
import com.igeeksky.xcache.core.CacheStore;
import com.igeeksky.xcache.core.statistic.CacheStatisticsCollector;
import com.igeeksky.xcache.core.statistic.CacheStatisticsHolder;
import com.igeeksky.xcache.core.statistic.CacheStatisticsPublisher;

import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * @author Patrick.Lau
 * @date 2020-12-11
 */
public interface CacheConfig<K, V> {

    String getName();

    String getNamespace();

    CacheLevel getCacheLevel();

    CacheStore<K, V> getCacheStore();

    Function<K, V> getLoader();

    CacheStatisticsHolder getCacheStatisticsHolder();

    BiPredicate<String, K> getContainsPredicate();

    /**
     * @return
     */
    CacheStatisticsPublisher getStatisticsPublisher();

}
