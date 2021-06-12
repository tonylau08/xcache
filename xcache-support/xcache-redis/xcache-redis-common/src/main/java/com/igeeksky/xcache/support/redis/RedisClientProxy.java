package com.igeeksky.xcache.support.redis;


import com.igeeksky.xcache.core.KeyValue;

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

    CompletableFuture<String> asyncGet(String key);
}
