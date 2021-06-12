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

    String get(String key);

    List<KeyValue<String, String>> mget(String... keys);

    String hget(String key, String field);

    void set(String key, String value);

    void mset(Map<String, String> keyValues);

    Long del(String... key);

    CompletableFuture<String> asyncGet(String key);

    CompletableFuture<List<KeyValue<String, String>>> asyncMget(String[] toStringKeys);

    CompletableFuture<Void> asyncSet(String key, String value);

    CompletableFuture<Void> asyncMset(Map<String, String> keyValues);

    CompletableFuture<Long> asyncDel(String... keys);

    Mono<String> reactiveGet(String key);

    Flux<KeyValue<String, String>> reactiveMget(String[] toStringKeys);

    Mono<Void> reactiveSet(String key, String value);

    Mono<Void> reactiveMset(Map<String, String> keyValues);

    Mono<Long> reactiveDel(String... keys);
}
