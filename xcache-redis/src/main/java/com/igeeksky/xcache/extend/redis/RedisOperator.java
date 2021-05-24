package com.igeeksky.xcache.extend.redis;

import com.igeeksky.xcache.core.KeyValue;

import java.util.List;

/**
 * @author Patrick.Lau
 * @date 2020-12-11
 */
public interface RedisOperator {

    public byte[] get(byte[] idBytes) ;

    public void setex(byte[] keyBytes, int expiration, byte[] valueBytes);

    void mset(List<KeyValue<byte[],byte[]>> keyValueList, int expiration);

    List<byte[]> mget(byte[][] keysArray);
}
