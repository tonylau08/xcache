package com.igeeksky.xcache.core;

import java.util.concurrent.CompletionStage;

/**
 * @author Patrick.Lau
 * @date 2021-05-24
 */
public interface AsyncCache {

    <T> CompletionStage<CacheStore.ValueWrapper<T>> asyncGet(Object key);

}
