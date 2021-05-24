/*
 * Copyright 2017 Tony.lau All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.igeeksky.xcache.extend.redis.connection;

import com.igeeksky.xcache.extend.redis.script.RedisScript;
import com.igeeksky.xcache.extend.redis.cmd.RedisCmd;
import com.igeeksky.xcache.extend.redis.cmd.*;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisNoScriptException;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Tony.Lau
 * @blog: https://my.oschina.net/xcafe
 * @createTime 2017-03-01 18:54:08
 */
public class JedisClient implements RedisSingleClient {

	RedisScript script;

    private final JedisPool pool;

    private final HostAndPort hostAndPort;

    public JedisClient(String host, JedisPoolConfig jedisPoolConfig) {
        hostAndPort = HostAndPort.parseString(host);
        pool = new JedisPool(jedisPoolConfig, hostAndPort.getHost(), hostAndPort.getPort());
    }

    private <T> T excute(RedisCmd<T> cmd) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return cmd.excute(jedis);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            if (null != jedis) jedis.close();
        }
    }

    public byte[] hget(final byte[] key, final byte[] field) {
        return excute(new RedisHget(key, field));
    }

    public byte[] get(final byte[] key) {
        return excute(new RedisGet(key));
    }

    @Override
    public Long hset(byte[] key, byte[] field, byte[] value) {
        return excute(new RedisHset(key, field, value));
    }

    @Override
    public Long hdel(byte[] key, byte[]... fields) {
        return excute(new RedisHdel(key, fields));
    }

    @Override
    public String set(byte[] key, byte[] value) {
        return excute(new RedisSet(key, value));
    }

    @Override
    public Long del(byte[] key) {
        return excute(new RedisDel(key));
    }

    @Override
    public Long del(byte[]... keys) {
        return excute(new RedisDelKeys(keys));
    }

    @Override
    public Map<byte[], byte[]> hgetAll(byte[] key) {
        return excute(new RedisHgetAll(key));
    }

    @Override
    public Set<byte[]> hkeys(byte[] key) {
        return excute(new RedisHkeys(key));
    }

    @Override
    public String setex(byte[] key, int seconds, byte[] value) {
        return excute(new RedisSetex(key, seconds, value));
    }

    public Object evalListKey(final RedisScript script, final List<byte[]> keys, final List<byte[]> args) {
        return eval(script, keys.size(), getParams(keys, args));
    }

    public Object evalOneKey(RedisScript putIfAbsentScript, final byte[]... params) {

		return eval(script, 1, params);
    }

    private byte[][] getParams(List<byte[]> keys, List<byte[]> args) {
        keys.addAll(args);
        return keys.toArray(new byte[keys.size()][]);
    }

    public Object eval(final RedisScript script, int keyCount, final byte[]... params) {
        Jedis jedis = pool.getResource();
        try {
            return jedis.evalsha(script.getShaBytes(), keyCount, params);
        } catch (JedisNoScriptException e) {
            return jedis.eval(script.getScriptBytes(), keyCount, params);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            if (null != jedis) jedis.close();
        }
    }

}
