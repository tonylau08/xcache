package com.igeeksky.xcache.test;

import com.igeeksky.xcache.core.*;
import com.igeeksky.xcache.support.caffeine.CaffeineCacheBuilder;
import com.igeeksky.xcache.support.redis.RedisCacheBuilder;
import com.igeeksky.xcache.support.redis.RedisWriter;
import com.igeeksky.xcache.support.redis.lettuce.LettuceRedisWriter;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import org.junit.jupiter.api.Test;

/**
 * @author: Patrick.Lau
 * @date: 2021-06-19
 */
public class XCacheTest {

    CompositeCacheManager cacheManager = cacheManager();

    public CompositeCacheManager cacheManager() {
        CacheBuilder caffeineCacheBuilder = new CaffeineCacheBuilder();

        RedisURI redisURI = RedisURI.builder().withHost("127.0.0.1").withPort(6379).build();
        RedisClient redisClient = RedisClient.create(redisURI);
        RedisWriter lettuceRedisWriter = new LettuceRedisWriter(redisClient);
        RedisCacheBuilder redisCacheBuilder = new RedisCacheBuilder(lettuceRedisWriter);

        DefaultCacheManager firstCacheManager = new DefaultCacheManager(caffeineCacheBuilder);
        DefaultCacheManager secondCacheManager = new DefaultCacheManager(redisCacheBuilder);
        return XCache.newBuilder().firstCacheManager(firstCacheManager).secondCacheManager(secondCacheManager).build();
    }

    @Test
    public void testSyncSetGetString() throws InterruptedException {
        cacheManager.setCacheUse("string", CacheUse.FIRST);
        Cache<String, String> cache = cacheManager.get("string", String.class, String.class);
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            String kv = "aa" + i;
            cache.sync().put(kv, kv);
        }
        long end = System.currentTimeMillis();
        System.out.println(end - start);
        for (int i = 0; i < 1000000; i++) {
            String kv = "aa" + i;
            CacheValue<String> value = cache.sync().get(kv);
            if ((i & 4095) == 0) {
                System.out.println(value.getValue());
            }
        }
        long end2 = System.currentTimeMillis();
        System.out.println(end2 - end);
    }

    @Test
    public void testSyncSetGetUser() throws InterruptedException {
        Cache<String, User> cache = cacheManager.get("User", String.class, User.class);
        cache.sync().put("bb", new User());
        CacheValue<User> cacheValue = cache.sync().get("bb");
        System.out.println(cacheValue.getValue());
        Thread.sleep(1000);
    }

    public static class User {
        private String name = "aa";
        private int age = 20;

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public int getAge() {
            return age;
        }

        @Override
        public String toString() {
            return "{\"name\":\"" + name + "\",\"age\":" + age + "}";
        }
    }

}
