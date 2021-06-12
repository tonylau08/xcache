package com.igeeksky.xcache.core.extend;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Patrick.Lau
 * @date 2020-12-10
 */
public class LocalLockSupport<K> implements LockSupport<K> {

    private static final int MAXIMUM_LOCK = 1 << 30;

    /**
     * LOCK数组对象的HashSlot计算
     */
    private int mask;

    private final int backSourceSize;

    /**
     * 内部锁的对象数组
     */
    private final ReentrantLock[] LOCK_ARRAY;

    public LocalLockSupport(int backSourceSize) {
        this.backSourceSize = lockSizeFor(backSourceSize == 0 ? 256 : backSourceSize);
        this.mask = this.backSourceSize - 1;
        this.LOCK_ARRAY = new ReentrantLock[this.backSourceSize];
        for (int i = 0; i < this.LOCK_ARRAY.length; i++) {
            LOCK_ARRAY[i] = new ReentrantLock();
        }
    }

    private int lockSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_LOCK) ? MAXIMUM_LOCK : n + 1;
    }

    @Override
    public Lock getLock(K key) {
        int hashCode = key.hashCode();
        return LOCK_ARRAY[hashCode & mask];
    }

}
