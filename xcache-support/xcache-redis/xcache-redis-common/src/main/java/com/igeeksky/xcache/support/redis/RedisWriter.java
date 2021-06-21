package com.igeeksky.xcache.support.redis;

import com.igeeksky.xcache.core.ExpiryKeyValue;
import com.igeeksky.xcache.core.KeyValue;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * @author Patrick.Lau
 * @date 2021-06-03
 */
public interface RedisWriter extends AutoCloseable {

    String OK = "OK";

    Mono<byte[]> get(byte[] key);

    Flux<KeyValue<byte[], byte[]>> mget(byte[]... keys);

    Mono<Void> set(byte[] key, byte[] value);

    Mono<Void> psetex(byte[] key, long milliseconds, byte[] value);

    Mono<Void> mset(Map<byte[], byte[]> keyValues);

    Mono<Void> mpsetex(ExpiryKeyValue<byte[], byte[]>... keyValues);

    Mono<Long> del(byte[]... keys);

    Mono<byte[]> hget(byte[] key, byte[] field);

    Flux<KeyValue<byte[], byte[]>> hmget(byte[] key, byte[]... field);

    Mono<Boolean> hset(byte[] key, byte[] field, byte[] value);

    Mono<Void> hmset(byte[] key, Map<byte[], byte[]> map);

    Mono<Long> hdel(byte[] key, byte[]... fields);

    Mono<Void> reactiveClose();

}
