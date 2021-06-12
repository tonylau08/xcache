package com.igeeksky.xcache.support.redis.lettuce;

import com.igeeksky.xcache.core.KeyValue;
import com.igeeksky.xcache.support.redis.RedisClientProxy;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Objects;

/**
 * @author Patrick.Lau
 * @date 2021-06-12
 */
public class LettuceRedisClient implements RedisClientProxy {

    private final Logger log = LoggerFactory.getLogger(LettuceRedisClient.class);

    private StatefulRedisConnection<String, String> connection;
    private RedisReactiveCommands<String, String> reactive;

    public LettuceRedisClient() {
        RedisClient redisClient = RedisClient.create();
        this.connection = redisClient.connect();
        this.reactive = connection.reactive();
    }

    @Override
    public Mono<String> get(String key) {
        return connection.reactive().get(key);
    }

    @Override
    public Flux<KeyValue<String, String>> mget(String... keys) {
        return connection.reactive()
                .mget(keys)
                .map(kv -> new KeyValue<>(kv.getKey(), kv.getValue()));
    }

    @Override
    public Mono<Void> set(String key, String value) {
        return connection.reactive()
                .set(key, value)
                .flatMap(result -> {
                    isSetSuccess(key, value, result);
                    return Mono.empty();
                });
    }

    @Override
    public Mono<Void> mset(Map<String, String> keyValues) {
        return connection.reactive()
                .mset(keyValues)
                .then();
    }

    @Override
    public Mono<Long> del(String... keys) {
        return connection.reactive().del(keys);
    }

    @Override
    public Mono<String> hget(String key, String field) {
        return connection.reactive().hget(key, field);
    }

    @Override
    public Flux<KeyValue<String, String>> hmget(String key, String... fields) {
        return connection.reactive()
                .hmget(key, fields)
                .map(kv -> new KeyValue<>(kv.getKey(), kv.getValue()));
    }

    @Override
    public Mono<Boolean> hset(String key, String field, String value) {
        return connection.reactive().hset(key, field, value);
    }

    @Override
    public Mono<Void> hmset(String key, Map<String, String> map) {
        // TODO 可能失败？
        return connection.reactive().hmset(key, map).then();
    }

    @Override
    public Mono<Long> hdel(String key, String... fields) {
        return connection.reactive().hdel(key, fields);
    }

    private void isSetSuccess(String key, String value, String result) {
        if (!Objects.equals(OK, result)) {
            RuntimeException e = new RuntimeException("redis set error. key=" + key + ", value=" + value);
            log.error(e.getMessage(), e);
            throw e;
        }
    }

}
