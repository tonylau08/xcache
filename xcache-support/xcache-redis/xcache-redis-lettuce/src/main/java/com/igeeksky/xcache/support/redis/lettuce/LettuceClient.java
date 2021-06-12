package com.igeeksky.xcache.support.redis.lettuce;


import com.igeeksky.xcache.core.KeyValue;
import com.igeeksky.xcache.core.util.CollectionUtils;
import com.igeeksky.xcache.support.redis.RedisClientProxy;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * @author Patrick.Lau
 * @date 2021-06-12
 */
public class LettuceClient implements RedisClientProxy {

    private final Logger log = LoggerFactory.getLogger(LettuceClient.class);

    private StatefulRedisConnection<String, String> connection;
    private RedisReactiveCommands<String, String> reactive;

    public LettuceClient() {
        RedisClient redisClient = RedisClient.create();
        this.connection = redisClient.connect();
        this.reactive = connection.reactive();
    }

    @Override
    public String get(String key) {
        return connection.sync().get(key);
    }

    @Override
    public List<KeyValue<String, String>> mget(String... keys) {
        List<io.lettuce.core.KeyValue<String, String>> keyValues = connection.sync().mget(keys);
        return toCacheKeyValues(keyValues);
    }

    @NotNull
    private List<KeyValue<String, String>> toCacheKeyValues(List<io.lettuce.core.KeyValue<String, String>> keyValues) {
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
        isSetSuccess(key, value, isOK);
    }

    private String isSetSuccess(String key, String value, String isOK) {
        if (!Objects.equals(OK, isOK)) {
            RuntimeException e = new RuntimeException("redis set error. key=" + key + ", value=" + value);
            log.error(e.getMessage(), e);
            throw e;
        }
        return isOK;
    }

    @Override
    public void mset(Map<String, String> keyValues) {
        connection.sync().mset(keyValues);
    }

    @Override
    public Long del(String... keys) {
        return connection.sync().del(keys);
    }

    @Override
    public CompletableFuture<String> asyncGet(String key) {
        return connection.async().get(key).toCompletableFuture();
    }

    @Override
    public CompletableFuture<List<KeyValue<String, String>>> asyncMget(String[] keys) {
        return connection.async().mget(keys).thenApply(this::toCacheKeyValues).toCompletableFuture();
    }

    @Override
    public CompletableFuture<Void> asyncSet(String key, String value) {
        return connection.async().set(key, value)
                .toCompletableFuture()
                .thenApply(result -> isSetSuccess(key, value, result))
                .thenApply(result -> null);
    }

    @Override
    public CompletableFuture<Void> asyncMset(Map<String, String> keyValues) {
        return connection.async().mset(keyValues).toCompletableFuture().thenApply(r -> null);
    }

    @Override
    public CompletableFuture<Long> asyncDel(String... keys) {
        return connection.async().del(keys).toCompletableFuture();
    }

    @Override
    public Mono<String> reactiveGet(String key) {
        return connection.reactive().get(key);
    }

    @Override
    public Flux<KeyValue<String, String>> reactiveMget(String[] toStringKeys) {
        return connection.reactive().mget(toStringKeys).map(kv -> new KeyValue(kv.getKey(), kv.getValue()));
    }

    @Override
    public Mono<Void> reactiveSet(String key, String value) {
        return connection.reactive().set(key, value).map(result -> {
            isSetSuccess(key, value, result);
            return null;
        });
    }

    @Override
    public Mono<Void> reactiveMset(Map<String, String> keyValues) {
        return connection.reactive().mset(keyValues).map(r -> null);
    }

    @Override
    public Mono<Long> reactiveDel(String... keys) {
        return connection.reactive().del(keys);
    }

}
