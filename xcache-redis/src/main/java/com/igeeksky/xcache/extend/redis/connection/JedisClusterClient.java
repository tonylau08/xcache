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
import com.igeeksky.xcache.extend.redis.cmd.*;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisMovedDataException;
import redis.clients.jedis.exceptions.JedisNoReachableClusterNodeException;
import redis.clients.jedis.exceptions.JedisNoScriptException;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author Tony.Lau
 * @blog: https://my.oschina.net/xcafe
 * @createTime 2017-03-01 18:50:04
 */
public class JedisClusterClient implements RedisClusterClient {

    private JedisClusterHandler jedisClusterHandler;

    private final int maxRedirects;

	private RedisScript script;

    private final RedisScript batchDelScript = new RedisScript("local num=0; for i in ipairs(ARGV) do num = num + redis.call('DEL',ARGV[i]); end; return num;");

    public JedisClusterClient(JedisClusterHandler jedisClusterHandler, int maxRedirects) {
        this.jedisClusterHandler = jedisClusterHandler;
        this.maxRedirects = maxRedirects;
    }

    private <T> T excute(RedisCmd<T> cmd, int redirect, Jedis jedis, Integer slot, HostAndPort targetNode) {
        if (redirect > maxRedirects) {
            return null;
        }
        try {
            if (redirect == 0) {
                jedis = jedisClusterHandler.getJedis(cmd.getKey());
            }
            return cmd.excute(jedis);
        } catch (JedisMovedDataException e) {
            slot = e.getSlot();
            targetNode = e.getTargetNode();
            jedis = jedisClusterHandler.refreshSlotAndGet(slot, targetNode);
            return excute(cmd, redirect + 1, jedis, slot, targetNode);
        } catch (JedisNoReachableClusterNodeException e) {
            jedis = jedisClusterHandler.getOtherHostJedis(cmd.getKey(), targetNode);
            return excute(cmd, redirect + 1, jedis, slot, targetNode);
        } finally {
            if (null != jedis) jedis.close();
        }
    }

    private <T> T excute(RedisCmd<T> cmd) {
        return excute(cmd, 0, null, null, null);
    }

    public byte[] hget(final byte[] key, final byte[] field) {
        return excute(new RedisHget(key, field));
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
    public Long hset(byte[] key, byte[] field, byte[] value) {
        return excute(new RedisHset(key, field, value));
    }

    @Override
    public Long hdel(final byte[] key, final byte[]... fields) {
        return excute(new RedisHdel(key, fields));
    }

    @Override
    public String set(byte[] key, byte[] value) {
        return excute(new RedisSet(key, value));
    }

    @Override
    public String setex(byte[] key, int second, byte[] value) {
        return excute(new RedisSetex(key, second, value));
    }

    @Override
    public byte[] get(final byte[] key) {
        return excute(new RedisGet(key));
    }

    @Override
    public Long del(byte[] key) {
        return excute(new RedisDel(key));
    }

	/*
	@Override
	public Long del(byte[] key) {
		return excute(new RedisCmd<Long>(){
			public Long excute(Jedis jedis){
				return jedis.del(key);
			}
			public byte[] getKey() {
				return key;
			}
		});
	}
	*/

    @Override
    public Long del(byte[]... keys) {

        Map<String, List<byte[]>> map = jedisClusterHandler.getHostsByKeys(keys);
        long delNum = 0l;

        Iterator<Entry<String, List<byte[]>>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, List<byte[]>> entry = it.next();
            String ipPort = entry.getKey();
            List<byte[]> list = entry.getValue();
            if (list.size() > 0) {
                delNum += (Long) evalOrEvalsha(0, null, null, ipPort, batchDelScript, 0, list.toArray(new byte[list.size()][]));
            }
        }
        return delNum;
    }

    @Override
    public Object evalOneKey(RedisScript putIfAbsentScript, final byte[]... params) {
        return evalOrEvalsha(0, null, null, jedisClusterHandler.getHost(params[0]), script, 1, params);
    }

    @Override
    public Object evalSameHostKeys(RedisScript script, String ipPort, final byte[] kvs) {
        return evalOrEvalsha(0, null, null, ipPort, script, 0, kvs);
    }

    @Override
    public String getHostByKey(byte[] key) {
        return jedisClusterHandler.getHost(key);
    }

    private Object evalOrEvalsha(int redirect, Integer slot, HostAndPort hp, String ipPort, RedisScript script, int keyCount, byte[]... params) {
        if (redirect > maxRedirects) {
            return null;
        }
        Jedis jedis = null;
        try {
            if (null == hp) {
                jedis = jedisClusterHandler.getJedis(ipPort);
            } else {
                jedis = jedisClusterHandler.refreshSlotAndGet(slot, hp);
            }
            return jedis.evalsha(script.getShaBytes(), keyCount, params);
        } catch (JedisNoScriptException e) {
            return jedis.eval(script.getScriptBytes(), keyCount, params);
        } catch (JedisMovedDataException e) {
            return evalOrEvalsha(redirect + 1, e.getSlot(), e.getTargetNode(), ipPort, script, keyCount, params);
        } finally {
            if (null != jedis) jedis.close();
        }
    }

}
