package com.igeeksky.xcache.core.sync;

import com.igeeksky.xcache.core.ValueWrapper;

/**
 * @author Patrick.Lau
 * @date 2020-12-11
 */
public abstract class AbstractSyncCache<K, V> implements SyncCache<K, V> {

    private final String name;

    protected final long expireAfterWrite;

    public AbstractSyncCache(String name, long expireAfterWrite) {
        this.name = name;
        this.expireAfterWrite = expireAfterWrite;
    }

    public ValueWrapper<V> get(K key) {
        return fromStore(key);
    }

    /**
     * 根据key从缓存中获取value
     *
     * @param key 缓存Key
     * @return 缓存值的包装类 <br>
     * <b>1. wrapper != null： </b><br>
     * 1.1 命中缓存且 wrapper.get() != null：未过期的缓存元素；<br>
     * 1.2 命中缓存但 wrapper.get() == null：未过期的空缓存元素（如允许缓存空元素以防止缓存穿透）。 <br><br>
     * <b>2. wrapper == null </b><br>
     * 2.1 未命中缓存<br>
     * 2.2 缓存元素过期 <br>
     * 情况2由业务端决定是否需要回源查询数据 <br>
     */
    protected abstract ValueWrapper<V> fromStore(K key);

    protected V fromValueWrapper(ValueWrapper<V> wrapper) {
        if (null == wrapper) {
            return null;
        }

        return wrapper.getValue();
    }

}
