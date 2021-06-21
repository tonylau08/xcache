package com.igeeksky.xcache.support.redis.lettuce;

import com.igeeksky.xcache.core.ExpiryKeyValue;
import com.igeeksky.xcache.core.KeyValue;
import com.igeeksky.xcache.support.redis.RedisWriter;
import io.lettuce.core.AbstractRedisClient;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.reactive.RedisHashReactiveCommands;
import io.lettuce.core.api.reactive.RedisKeyReactiveCommands;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import io.lettuce.core.api.reactive.RedisStringReactiveCommands;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.reactive.RedisAdvancedClusterReactiveCommands;
import io.lettuce.core.codec.ByteArrayCodec;
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
public class LettuceRedisWriter implements RedisWriter {

    private final Logger log = LoggerFactory.getLogger(LettuceRedisWriter.class);

    private AbstractRedisClient abstractRedisClient;
    private StatefulConnection<byte[], byte[]> statefulConnection;
    private RedisStringReactiveCommands<byte[], byte[]> stringReactiveCommands;
    private RedisKeyReactiveCommands<byte[], byte[]> keyReactiveCommands;
    private RedisHashReactiveCommands<byte[], byte[]> hashReactiveCommands;

    public LettuceRedisWriter(AbstractRedisClient abstractRedisClient) {
        this.abstractRedisClient = abstractRedisClient;
        if (abstractRedisClient instanceof RedisClusterClient) {
            RedisClusterClient redisClusterClient = (RedisClusterClient) abstractRedisClient;
            StatefulRedisClusterConnection<byte[], byte[]> connection = redisClusterClient.connect(new ByteArrayCodec());
            this.statefulConnection = connection;
            RedisAdvancedClusterReactiveCommands<byte[], byte[]> reactiveCommands = connection.reactive();
            this.stringReactiveCommands = reactiveCommands;
            this.keyReactiveCommands = reactiveCommands;
            this.hashReactiveCommands = reactiveCommands;
        } else if (abstractRedisClient instanceof RedisClient) {
            RedisClient redisClient = (RedisClient) abstractRedisClient;
            StatefulRedisConnection<byte[], byte[]> connection = redisClient.connect(new ByteArrayCodec());
            this.statefulConnection = connection;
            RedisReactiveCommands<byte[], byte[]> reactiveCommands = connection.reactive();
            this.stringReactiveCommands = reactiveCommands;
            this.keyReactiveCommands = reactiveCommands;
            this.hashReactiveCommands = reactiveCommands;
        } else {
            throw new UnsupportedOperationException("Unsupported Redis Client.");
        }
    }

    @Override
    public Mono<byte[]> get(byte[] key) {
        return stringReactiveCommands.get(key);
    }

    @Override
    public Flux<KeyValue<byte[], byte[]>> mget(byte[]... keys) {
        return stringReactiveCommands.mget(keys)
                .filter(kv -> kv.hasValue())
                .map(kv -> new KeyValue<>(kv.getKey(), kv.getValue()));
    }

    @Override
    public Mono<Void> set(byte[] key, byte[] value) {
        return stringReactiveCommands.set(key, value)
                .flatMap(result -> {
                    isSetSuccess(key, value, result);
                    return Mono.empty();
                });
    }

    @Override
    public Mono<Void> psetex(byte[] key, long milliseconds, byte[] value) {
        return stringReactiveCommands.psetex(key, milliseconds, value).flatMap(result -> {
            isSetSuccess(key, value, result);
            return Mono.empty();
        });
    }

    @Override
    public Mono<Void> mset(Map<byte[], byte[]> keyValues) {
        return stringReactiveCommands.mset(keyValues).then();
    }

    @Override
    public final Mono<Void> mpsetex(ExpiryKeyValue<byte[], byte[]>... keyValues) {
        if (keyValues.length < 2) {
            ExpiryKeyValue<byte[], byte[]> keyValue = keyValues[0];
            return psetex(keyValue.getKey(), keyValue.getTtl().toMillis(), keyValue.getValue());
        }
        Flux<String> flux = Flux.empty();
        for (ExpiryKeyValue<byte[], byte[]> keyValue : keyValues) {
            Mono<String> mono = stringReactiveCommands.psetex(keyValue.getKey(), keyValue.getTtl().toMillis(), keyValue.getValue());
            flux = flux.concatWith(mono);
        }
        return flux.then();
    }

    @Override
    public Mono<Long> del(byte[]... keys) {
        return keyReactiveCommands.del(keys);
    }

    @Override
    public Mono<byte[]> hget(byte[] key, byte[] field) {
        return hashReactiveCommands.hget(key, field);
    }

    @Override
    public Flux<KeyValue<byte[], byte[]>> hmget(byte[] key, byte[]... fields) {
        return hashReactiveCommands.hmget(key, fields)
                .map(kv -> new KeyValue<>(kv.getKey(), kv.getValue()));
    }

    @Override
    public Mono<Boolean> hset(byte[] key, byte[] field, byte[] value) {
        return hashReactiveCommands.hset(key, field, value);
    }

    @Override
    public Mono<Void> hmset(byte[] key, Map<byte[], byte[]> map) {
        // TODO 可能失败？
        return hashReactiveCommands.hmset(key, map).then();
    }

    @Override
    public Mono<Long> hdel(byte[] key, byte[]... fields) {
        return hashReactiveCommands.hdel(key, fields);
    }

    @Override
    public void close() throws Exception {
        reactiveClose().subscribe();
    }

    @Override
    public Mono<Void> reactiveClose() {
        if (null != statefulConnection) {
            return Mono.fromFuture(statefulConnection.closeAsync().thenCompose(v -> abstractRedisClient.shutdownAsync()));
        }
        return Mono.empty();
    }

    private void isSetSuccess(byte[] key, byte[] value, String result) {
        if (!Objects.equals(OK, result)) {
            RuntimeException e = new RuntimeException("redis set error. key=" + key + ", value=" + value);
            log.error(e.getMessage(), e);
            throw e;
        }
    }

}
