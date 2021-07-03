package com.igeeksky.xcache.core.statistic;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Patrick.Lau
 * @date 2021-06-25
 */
public class CacheStatisticsCollector {

    private final Set<CacheStatisticsHolder> holders = new HashSet<>();

    private final CacheStatisticsPublisher publisher;
    private final ScheduledExecutorService executorService;

    public CacheStatisticsCollector(CacheStatisticsPublisher publisher) {
        this.publisher = publisher;
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(new PublishTask(this), 1, 1, TimeUnit.SECONDS);
    }

    public CacheStatisticsHolder getCacheStatisticsHolder(String namespace, String application, String name) {
        CacheStatisticsHolder holder = new CacheStatisticsHolder(namespace, application, name);
        synchronized (holders) {
            holders.add(holder);
        }
        return holder;
    }

    private static class PublishTask implements Runnable {

        private final CacheStatisticsCollector collector;

        public PublishTask(CacheStatisticsCollector collector) {
            this.collector = collector;
        }

        @Override
        public void run() {
            List<CacheStatisticsMessage> messages = this.collector.holders.stream().map(CacheStatisticsHolder::collect).collect(Collectors.toList());
            this.collector.publisher.publish(messages);
        }
    }

}
