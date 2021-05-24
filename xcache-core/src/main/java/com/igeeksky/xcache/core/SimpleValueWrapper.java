package com.igeeksky.xcache.core;

public class SimpleValueWrapper implements CacheStore.ValueWrapper {

    private final Object value;

    public SimpleValueWrapper(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return this.value;
    }
}