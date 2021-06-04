package com.igeeksky.xcache.support.redis;

import com.igeeksky.xcache.core.extend.Serializer;
import com.igeeksky.xcache.core.util.BytesUtils;

import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * @author Patrick.Lau
 */
public class RedisKeyMetadata<K> {

    private final Charset charset;

    /**
     * 名称
     */
    private final String name;

    /**
     * Key的前缀：name + "~I:"
     * Redis类型：string
     */
    private final String keyPrefix;

    /**
     *
     */
    private final byte[] nameBytes;

    /**
     *
     */
    private final byte[] keyPrefixBytes;

    private Serializer<K> keySerializer;

    public RedisKeyMetadata(String name, Charset charset, Serializer<K> keySerializer) {
        this.name = name;
        this.charset = charset;
        this.keyPrefix = name + "~I:";
        this.nameBytes = name.getBytes(charset);
        this.keyPrefixBytes = keyPrefix.getBytes(charset);
        this.keySerializer = keySerializer;
    }

    /**
     * 缓存名称
     */
    public String getName() {
        return name;
    }

    /**
     * 缓存名称的字节数组
     */
    public byte[] getNameBytes() {
        return nameBytes;
    }

    /**
     * 以ID为key的缓存前缀
     */
    public String getKeyPrefix() {
        return keyPrefix;
    }

    /**
     * 以ID为key的缓存前缀
     */
    public byte[] getKeyPrefixBytes() {
        return keyPrefixBytes;
    }

    /**
     * 获取去除前缀之后的ID bytes
     */
    public byte[] getKeySuffixBytes(byte[] fullKeyBytes) {
        if (fullKeyBytes.length < keyPrefixBytes.length) {
            throw new IllegalArgumentException("keyBytes less than pfefix bytes: " + new String(fullKeyBytes));
        }
        return Arrays.copyOfRange(fullKeyBytes, keyPrefixBytes.length, fullKeyBytes.length);
    }

    /**
     * 获取全部的缓存名称
     */
    public byte[] getKeyFullBytes(K key) {
        return BytesUtils.merge(keyPrefixBytes, keySerializer.serialize(key));
    }

}