package com.igeeksky.xcache.core.statistic;

import java.util.concurrent.atomic.LongAdder;

/**
 * @author Patrick.Lau
 * @date 2020-12-12
 */
public class CacheStatisticsCounter implements CacheStatistics {

    private String name;
    private final LongAdder hits;
    private final LongAdder misses;
    private LongAdder loadSuccessCount;
    private LongAdder loadFailureCount;
    private LongAdder totalLoadTime;

    public CacheStatisticsCounter(String name) {
        this.name = name;
        this.hits = new LongAdder();
        this.misses = new LongAdder();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getHits() {
        return hits.sum();
    }

    @Override
    public void incHits() {
        hits.increment();
    }

    @Override
    public long getMisses() {
        return misses.sum();
    }

    @Override
    public void incMisses() {
        misses.increment();
    }
}
