package com.igeeksky.xcache.core.statistic;

import java.util.List;

/**
 * @author Patrick.Lau
 * @date 2021-06-26
 */
public class NoOpCacheStatisticsPublisher implements CacheStatisticsPublisher {

    @Override
    public boolean publish(List<CacheStatisticsMessage> messages) {
        messages.forEach(message -> {
            //logger.debug(message.toString());
        });
        return true;
    }
}
