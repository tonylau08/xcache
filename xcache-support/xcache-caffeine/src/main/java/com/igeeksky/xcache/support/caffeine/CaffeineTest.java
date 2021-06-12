package com.igeeksky.xcache.support.caffeine;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.concurrent.*;

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
    public void testAsync() throws ExecutionException, InterruptedException {
        CompletableFuture<String> future = CompletableFuture.completedFuture("aa");
        testFuture(future).thenAccept(s -> {
            System.out.println("END:" + s);
        }).get();
    }

    private CompletableFuture<String> testFuture(CompletableFuture<String> future) {
        return future.thenCompose(s -> {
            return sa().thenApply(v -> {
                return s;
            });
        });
    }

    public CompletableFuture<Void> sa() {
        return CompletableFuture.runAsync(() -> {
            try {
                throw new RuntimeException("error");
            } catch (Exception e) {

            }
        });
    }


    /**
     *
     */
    @Test
    public void testFrequencyDelete() throws InterruptedException {
        AsyncCache<Object, Object> cache = Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(128)
                .buildAsync();


        for (int i = 0; i < 20; i++) {
            Random random = ThreadLocalRandom.current();
            for (int j = 0; j < 10000; j++) {
                cache.synchronous().put("a" + j, "a" + random.nextInt(12800));
            }
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
