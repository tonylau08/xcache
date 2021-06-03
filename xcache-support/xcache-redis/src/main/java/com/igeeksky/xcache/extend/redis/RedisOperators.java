package com.igeeksky.xcache.extend.redis;

import java.util.concurrent.CompletionStage;

/**
 * @author Patrick.Lau
 * @date 2021-06-03
 */
public interface RedisOperators {

    String get(String key);

    CompletionStage<String> asyncGet(String key);

}
