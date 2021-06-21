package com.igeeksky.xcache.support.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.igeeksky.xcache.core.CacheValue;
import com.igeeksky.xcache.core.ExpiryCacheValue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * @author: Patrick.Lau
 * @date: 2021-06-16
 */
class CaffeineCacheStoreTest {

    Cache<String, ExpiryCacheValue<String>> cache =
            Caffeine.newBuilder()
                    .expireAfter(new RandomRangeCacheExpiry<String, String>(10000000000L, 5000000000L))
                    .maximumSize(128)
                    .build();

    CaffeineCacheStore<String, String> cacheStore
            = new CaffeineCacheStore<>(cache);

    @Test
    void get() throws InterruptedException {
        String key = "a";
        String value = "a";
        cacheStore.put(key, value).block();
        Thread.sleep(1000);
        CacheValue<String> cacheValue = cacheStore.get(key).block();
        Assertions.assertNotNull((null != cacheValue ? cacheValue.getValue() : null), "01");
        Thread.sleep(4500);
        cacheValue = cacheStore.get("a").block();
        Assertions.assertNotNull((null != cacheValue ? cacheValue.getValue() : null), "02");
        Thread.sleep(3500);
        cacheValue = cacheStore.get("a").block();
        Assertions.assertNotNull((null != cacheValue ? cacheValue.getValue() : null), "03");
        Thread.sleep(1000);
        cacheValue = cacheStore.get("a").block();
        Assertions.assertNull((null != cacheValue ? cacheValue.getValue() : null), "04");
    }

    @Test
    void getAll() {
        LinkedHashSet<String> keySet = new LinkedHashSet(128);
        cacheStore.putAll(Mono.fromSupplier(() -> {
            String prefix = "a";
            Map<String, String> map = new HashMap<>(128);
            for (int i = 0; i < 100; i++) {
                map.put(prefix + i, prefix + i);
                keySet.add(prefix + i);
            }
            return map;
        })).subscribe();

        cacheStore.getAll(keySet)
                .subscribe(kv -> {
                    System.out.println(kv.getKey() + " : " + kv.getValue().getValue());
                });

    }

    @Test
    void put() {

    }

    @Test
    void putAll() {
    }

    @Test
    void remove() {
    }

    @Test
    void clear() {
    }


    @BeforeEach
    void setUp() {
        System.out.println("start-------------");
    }

    @AfterEach
    void tearDown() {
        System.out.println("end-------------");
    }

}