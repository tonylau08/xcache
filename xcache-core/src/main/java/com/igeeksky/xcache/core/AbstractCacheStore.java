package com.igeeksky.xcache.core;

import java.util.Objects;

/**
 *
 * @author Patrick.Lau
 * @date 2020-12-11
 */
public abstract class AbstractCacheStore implements CacheStore, AsyncCache {

    private final String name;

    protected final long expireAfterWrite;

    public AbstractCacheStore(String name, long expireAfterWrite) {
        this.name = name;
        this.expireAfterWrite = expireAfterWrite;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public <T> ValueWrapper<T> get(Object key) {
        return fromStore(key);
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        Objects.requireNonNull(key, "Class Type must not be null");

        ValueWrapper<T> wrapper = fromStore(key);
        return fromValueWrapper(wrapper);
    }

    /**
     * 根据key从缓存中获取value
     * @param key 缓存Key
     * @return 缓存值的包装类 <br>
     * <b>1. wrapper != null： </b><br>
     *     1.1 命中缓存且 wrapper.get() != null：未过期的缓存元素；<br>
     *     1.2 命中缓存但 wrapper.get() == null：未过期的空缓存元素（如允许缓存空元素以防止缓存穿透）。 <br><br>
     * <b>2. wrapper == null </b><br>
     *     2.1 未命中缓存<br>
     *     2.2 缓存元素过期 <br>
     *     情况2由业务端决定是否需要回源查询数据 <br>
     */
    protected abstract  <T> ValueWrapper<T> fromStore(Object key);

    protected  <T> T fromValueWrapper(ValueWrapper<T> wrapper) {
        if (null == wrapper) {
            return null;
        }

        return wrapper.getValue();
    }

}
