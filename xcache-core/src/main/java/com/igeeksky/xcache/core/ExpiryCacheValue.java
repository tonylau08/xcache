package com.igeeksky.xcache.core;

/**
 * @author: Patrick.Lau
 * @date: 2021-06-16
 */
public class ExpiryCacheValue<V> extends SimpleCacheValue<V> {

    private long expiryTime;

    public ExpiryCacheValue(V value) {
        super(value);
    }

    public long getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(long expiryTime) {
        this.expiryTime = expiryTime;
    }
}
