package com.igeeksky.xcache.core.extend;

import java.util.concurrent.locks.Lock;

/**
 * @author Patrick.Lau
 * @date 2021-06-10
 */
public interface LockSupport<K> {

    /**
     * 获取锁
     *
     * @param key
     * @return
     */
    Lock getLock(K key);

}
