package com.igeeksky.xcache.core.statistic;

/**
 * @author: Patrick.Lau
 * @date: 2021-06-11
 */
public interface CacheStatistics {

    String getName();

    long getHits();

    void incHits();

    long getMisses();

    void incMisses();

}
