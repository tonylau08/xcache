package com.igeeksky.xcache.core.statistic;

import com.igeeksky.xcache.core.CacheValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.support.NoOpCache;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Patrick.Lau
 * @date 2021-06-11
 */
public class CacheStatisticsHolder {

    private final Logger log = LoggerFactory.getLogger(CacheStatisticsHolder.class);

    private final String namespace;
    private final String application;
    private final String name;

    private final AtomicReference<CacheStatisticsCounter> reference = new AtomicReference<>(new CacheStatisticsCounter());

    public CacheStatisticsHolder(String namespace, String application, String name) {
        this.namespace = namespace;
        this.application = application;
        this.name = name;
    }

    public <V> void recordGets(CacheValue<V> cacheValue) {
        if (null == cacheValue) {
            reference.get().incMisses();
            return;
        }
        V value = cacheValue.getValue();
        if (null != value) {
            reference.get().incHitsNotNull();
        } else {
            reference.get().incHitsNull();
        }
    }

    public CacheStatisticsMessage collect() {
        CacheStatisticsCounter counter = updateCounter();
        CacheStatisticsMessage message = new CacheStatisticsMessage(namespace, application, name);
        message.setNotNullHits(counter.getNotNullHits());
        message.setNullHits(counter.getNullHits());
        message.setMisses(counter.getMisses());
        log.debug(message.toString());
        return message;
    }

    private CacheStatisticsCounter updateCounter() {
        CacheStatisticsCounter expectedCounter = reference.get();
        CacheStatisticsCounter newCounter = new CacheStatisticsCounter();
        if (reference.compareAndSet(expectedCounter, newCounter)) {
            return expectedCounter;
        }
        return updateCounter();
    }

}
