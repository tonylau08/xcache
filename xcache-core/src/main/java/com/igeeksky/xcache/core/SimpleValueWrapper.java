package com.igeeksky.xcache.core;

public class SimpleValueWrapper<V> implements Cache.ValueWrapper {

    private final V value;

    public SimpleValueWrapper(V value) {
        this.value = value;
    }

    public V getValue() {
        return this.value;
    }
}