package com.igeeksky.xcache.extend.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * @author Patrick.Lau
 * @date 2020-12-12
 */
public class CaffeineTest {

    @Test
    public void testSAsync() {
        Semaphore semaphore = new Semaphore(1);

        try {
            semaphore.acquire();

            int permits = semaphore.availablePermits();
            System.out.println(permits);

            semaphore.release();
            permits = semaphore.availablePermits();
            System.out.println(permits);

            semaphore.release();
            permits = semaphore.availablePermits();
            System.out.println(permits);

            boolean tryAcquire = semaphore.tryAcquire();
            System.out.println(tryAcquire);

            tryAcquire = semaphore.tryAcquire();
            System.out.println(tryAcquire);

            tryAcquire = semaphore.tryAcquire();
            System.out.println(tryAcquire);


        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testAsync() {

    }

    /**
     *
     */
    @Test
    public void testFrequencyDelete() throws InterruptedException {
        Cache<Object, Object> cache = Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(128)
                .build();


        for (int i = 0; i < 20; i++) {
            new Thread(() -> {
                Random random = ThreadLocalRandom.current();
                for (int j = 0; j < 10000; j++) {
                    cache.put("a" + random.nextInt(12800), "a" + random.nextInt(12800));
                }
            }).start();
        }

        for (int i = 0; i < 20; i++) {
            new Thread(() -> {
                Random random = ThreadLocalRandom.current();
                for (int j = 0; j < 128; j++) {
                    cache.getIfPresent("a" + random.nextInt(12800));
                }
            }).start();
        }

        Thread.sleep(1000000);

    }


}
