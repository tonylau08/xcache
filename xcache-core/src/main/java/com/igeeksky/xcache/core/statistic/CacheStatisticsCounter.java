package com.igeeksky.xcache.core.statistic;

import java.util.concurrent.atomic.LongAdder;

/**
 * @author Patrick.Lau
 * @date 2020-12-12
 */
public class CacheStatisticsCounter {

    private final LongAdder nullHits;
    private final LongAdder notNullHits;
    private final LongAdder misses;

    public CacheStatisticsCounter() {
        this.nullHits = new LongAdder();
        this.notNullHits = new LongAdder();
        this.misses = new LongAdder();
    }

    public long getNullHits() {
        return nullHits.sum();
    }

    public long getNotNullHits() {
        return notNullHits.sum();
    }

    public long getMisses() {
        return misses.sum();
    }

    public void incHitsNull() {
        nullHits.increment();
    }

    public void incHitsNotNull() {
        notNullHits.increment();
    }

    public void incMisses() {
        misses.increment();
    }

}
