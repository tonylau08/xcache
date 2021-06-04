package com.igeeksky.xcache.core;

import com.igeeksky.xcache.core.async.AsyncCache;
import com.igeeksky.xcache.core.reactive.ReactiveCache;
import com.igeeksky.xcache.core.sync.SyncCache;

/**
 * @author Patrick.Lau
 * @date 2021-06-04
 */
public interface Xcache<K, V> {

    SyncCache<K, V> sync();

    AsyncCache<K, V> async();

    ReactiveCache<K, V> reactive();

}
