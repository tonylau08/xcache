package com.igeeksky.xcache.core;

import com.igeeksky.xcache.core.extend.SerializerMapper;
import com.igeeksky.xcache.core.extend.compress.Compressor;
import com.igeeksky.xcache.core.extend.compress.GzipCompressor.GzipCompressorSupplier;
import com.igeeksky.xcache.core.statistic.CacheStatisticsPublisher;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * @author Patrick.Lau
 * @date 2021-06-23
 */
public class DefaultCacheBuilder implements CacheBuilder {

    private SerializerMapper keySerializerMapper;
    private SerializerMapper valueSerializerMapper;
    private Supplier<Compressor> compressorSupplier = new GzipCompressorSupplier();
    private CacheStatisticsPublisher cacheStatisticsPublisher;
    private Duration expireAfterCreate;
    private Duration expireAfterRead;
    private boolean allowNullValue = true;
    private boolean serializeValue = false;
    private boolean compressValue = false;


    public DefaultCacheBuilder allowNullValue(boolean allowNullValue) {
        this.allowNullValue = allowNullValue;
        return this;
    }

    public DefaultCacheBuilder serializeValue(boolean serializeValue) {
        this.serializeValue = serializeValue;
        return this;
    }

    public DefaultCacheBuilder compressValue(boolean compressValue) {
        this.compressValue = compressValue;
        return this;
    }

    public DefaultCacheBuilder cacheStatisticsPublisher(CacheStatisticsPublisher cacheStatisticsPublisher) {
        this.cacheStatisticsPublisher = cacheStatisticsPublisher;
        return this;
    }

    public DefaultCacheBuilder compressorSupplier(Supplier<Compressor> compressorSupplier) {
        this.compressorSupplier = compressorSupplier;
        return this;
    }

    public DefaultCacheBuilder expireAfterCreate(Duration expireAfterCreate) {
        this.expireAfterCreate = expireAfterCreate;
        return this;
    }

    public DefaultCacheBuilder expireAfterRead(Duration expireAfterRead) {
        this.expireAfterRead = expireAfterRead;
        return this;
    }

    public DefaultCacheBuilder keySerializerFunction(SerializerMapper keySerializerMapper) {
        this.keySerializerMapper = keySerializerMapper;
        return this;
    }

    public DefaultCacheBuilder valueSerializerFunction(SerializerMapper valueSerializerMapper) {
        this.valueSerializerMapper = valueSerializerMapper;
        return this;
    }

    @Override
    public Cache<Object, Object> build(String name) {
        return null;
    }

    @Override
    public <K, V> Cache<K, V> build(String name, Class<K> keyClazz, Class<V> valueClazz) {
        return null;
    }
}
