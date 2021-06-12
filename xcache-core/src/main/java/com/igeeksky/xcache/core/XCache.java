package com.igeeksky.xcache.core;


/**
 * @author Patrick.Lau
 * @date 2021-06-04
 */
public interface XCache<K, V> extends SyncCache<K, V>, AsyncCache<K, V>, ReactiveCache<K, V> {

    String getName();

}
