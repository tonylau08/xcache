package com.igeeksky.xcache.core.extend;

import java.util.List;

/**
 * @author Patrick.Lau
 * @date 2020-12-10
 */
public class LockSupport<T> {

    static final int MAXIMUM_LOCK = 1 << 30;

    /**
     * LOCK数组对象的HashSlot计算
     */
    private int mask;

    private final int backSourceSize;

    /**
     * 内部锁的对象数组
     */
    private final Lock[] LOCK_ARRAY;

    private final Lock GLOBAL_LOCK = new Lock();

    public LockSupport(int backSourceSize) {
        this.backSourceSize = lockSizeFor(backSourceSize == 0 ? 256 : backSourceSize);
        this.mask = this.backSourceSize - 1;
        this.LOCK_ARRAY = new Lock[this.backSourceSize];
        for (int i = 0; i < this.LOCK_ARRAY.length; i++) {
            LOCK_ARRAY[i] = new Lock();
        }
    }

    public final int lockSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_LOCK) ? MAXIMUM_LOCK : n + 1;
    }

    public Lock getLockByKeys(List<T> keys) {
        int size = keys.size();
        if (size < backSourceSize >> 1) {
            return getLockByKey(keys.get(0));
        }

        int maxFrequency = 0;
        int index = 0;
        int[] indexArray = new int[backSourceSize];
        for (Object key : keys) {
            int hashCode = key.hashCode();
            int i = hashCode & mask;
            int frequency = indexArray[i] + 1;
            indexArray[i] = frequency;
            if (maxFrequency >= frequency) {
                continue;
            }
            maxFrequency = frequency;
            index = i;
        }

        return LOCK_ARRAY[index];
    }

    public Lock getLockByKey(T key) {
        int hashCode = key.hashCode();
        return LOCK_ARRAY[hashCode & mask];
    }

    public Object getGlobalLock() {
        return GLOBAL_LOCK;
    }

}
