package com.igeeksky.xcache.core;

import com.igeeksky.xcache.core.statistic.CacheStatisticsHolder;

import java.util.function.Function;

/**
 * @author Patrick.Lau
 * @date 2020-12-11
 */
public interface CacheConfig<K, V> {

    String getName();

    CacheStore<K, V> getCacheStore();

    Function<K, V> getLoader();

    CacheStatisticsHolder getStatisticsHolder();

}
