package com.igeeksky.xcache.support.redis;


import com.igeeksky.xcache.core.KeyValue;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author Patrick.Lau
 * @date 2021-06-03
 */
public interface RedisClientProxy {

    static final String OK = "OK";

    Mono<String> get(String key);

    Flux<KeyValue<String, String>> mget(String... keys);

    Mono<Void> set(String key, String value);

    Mono<Void> mset(Map<String, String> keyValues);

    Mono<Long> del(String... keys);

    Mono<String> hget(String key, String field);

    Flux<KeyValue<String, String>> hmget(String key, String... field);

    Mono<Boolean> hset(String key, String field, String value);

    Mono<Void> hmset(String key, Map<String, String> map);

    Mono<Long> hdel(String key, String... fields);

}
