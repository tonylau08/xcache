package com.igeeksky.xcache.core;


/**
 * @author Patrick.Lau
 * @date 2021-06-04
 */
public interface Cache<K, V> extends ReactiveCache<K, V> {

    String getName();

    CacheLevel getCacheLevel();

    SyncCache<K, V> sync();

    AsyncCache<K, V> async();

}
