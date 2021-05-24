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

import java.util.Map;
import java.util.Set;

/**
 * @author Tony.Lau
 * @blog: https://my.oschina.net/xcafe
 * @createTime 2017-02-24 02:01:58
 */
public interface RedisClient {

    public String set(final byte[] key, final byte[] value);

    public byte[] get(final byte[] key);

    public String setex(byte[] key, int second, byte[] value);

    public Long del(byte[] key);

    public Long del(byte[]... keys);

    public Long hset(final byte[] key, final byte[] field, final byte[] value);

    public byte[] hget(final byte[] key, final byte[] field);

    public Long hdel(final byte[] key, byte[]... fields);

    public Map<byte[], byte[]> hgetAll(byte[] key);

    public Set<byte[]> hkeys(byte[] key);

    public Object evalOneKey(RedisScript putIfAbsentScript, final byte[]... params);

}
