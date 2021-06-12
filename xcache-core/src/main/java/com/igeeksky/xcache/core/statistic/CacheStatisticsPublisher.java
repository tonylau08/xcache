package com.igeeksky.xcache.core.statistic;

/**
 * @author: Patrick.Lau
 * @date: 2021-06-11
 */
public interface CacheStatisticsPublisher {

    boolean publish(CacheStatistics cacheStatistics);

}
