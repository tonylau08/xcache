package com.igeeksky.xcache.support.redis.lettuce;


import com.igeeksky.xcache.core.KeyValue;
import com.igeeksky.xcache.core.util.CollectionUtils;
import com.igeeksky.xcache.support.redis.RedisClientProxy;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.StatefulRedisConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * @author Patrick.Lau
 * @date 2021-06-12
 */
public class LettuceClient implements RedisClientProxy {

    private final Logger log = LoggerFactory.getLogger(LettuceClient.class);

    private StatefulRedisConnection<String, String> connection;

    public LettuceClient() {
        RedisClient redisClient = RedisClient.create();
        this.connection = redisClient.connect();
    }

    @Override
    public String get(String key) {
        return connection.sync().get(key);
    }

    @Override
    public List<KeyValue<String, String>> mget(String... keys) {
        List<io.lettuce.core.KeyValue<String, String>> keyValues = connection.sync().mget(keys);
        if (CollectionUtils.isNotEmpty(keyValues)) {
            List<KeyValue<String, String>> results = new ArrayList<>(keyValues.size());
            for (io.lettuce.core.KeyValue<String, String> keyValue : keyValues) {
                results.add(new KeyValue(keyValue.getKey(), keyValue.getValue()));
            }
            return results;
        }
        return Collections.emptyList();
    }

    @Override
    public String hget(String key, String field) {
        return connection.sync().hget(key, field);
    }

    @Override
    public void set(String key, String value) {
        String isOK = connection.sync().set(key, value);
        if (!Objects.equals(OK, isOK)) {
            RuntimeException e = new RuntimeException("redis set error. key=" + key + ", value=" + value);
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void mset(Map<String, String> keyValues) {
        connection.sync().mset(keyValues);
    }

    @Override
    public CompletableFuture<String> asyncGet(String key) {
        return connection.async().get(key).toCompletableFuture();
    }
}
