package com.igeeksky.xcache.core.config;

import com.igeeksky.xcache.core.CacheUse;
import com.igeeksky.xcache.core.extend.LockSupport;
import com.igeeksky.xcache.core.extend.SerializerMapper;
import com.igeeksky.xcache.core.extend.compress.Compressor;
import com.igeeksky.xcache.core.extend.compress.GzipCompressor.GzipCompressorSupplier;
import com.igeeksky.xcache.core.statistic.CacheStatisticsPublisher;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * @author Patrick.Lau
 * @date 2021-06-24
 */
public class CacheStoreConfig {

    /**
     * Cache（获取锁）
     */
    private LockSupport<?> lockSupport;

    /**
     * CacheStore
     */
    private SerializerMapper keySerializerMapper;

    /**
     * CacheStore
     */
    private SerializerMapper valueSerializerMapper;

    /**
     * CacheStore
     */
    private Supplier<Compressor> compressorSupplier = new GzipCompressorSupplier();

    /**
     * Cache(统计上报)
     */
    private CacheStatisticsPublisher cacheStatisticsPublisher;

    /**
     * CacheStore
     */
    private Duration expireAfterCreate;

    /**
     * LocalCacheStore
     */
    private Duration expireAfterRead;

    /**
     * Cache（空值过滤）
     */
    private boolean allowNullValue = true;

    /**
     * CacheStore
     */
    private boolean serializeValue = false;

    /**
     * CacheStore
     */
    private boolean compressValue = false;

    /**
     * CacheStore
     */
    private boolean refresh = false;

    /**
     * CacheUse
     */
    private CacheUse cacheUse = CacheUse.BOTH;
}
