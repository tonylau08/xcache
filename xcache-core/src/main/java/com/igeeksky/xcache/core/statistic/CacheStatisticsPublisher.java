package com.igeeksky.xcache.core.statistic;

import java.util.List;

/**
 * @author Patrick.Lau
 * @date 2021-06-11
 */
public interface CacheStatisticsPublisher {

    
    boolean publish(List<CacheStatisticsMessage> messages);

}
