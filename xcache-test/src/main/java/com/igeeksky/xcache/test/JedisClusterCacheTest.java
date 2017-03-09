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

package com.igeeksky.xcache.test;

import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.igeeksky.xcache.extend.redis.RedisClusterCache;
import com.igeeksky.xcache.support.redis.JedisClusterClient;
import com.igeeksky.xcache.support.redis.JedisClusterHandler;
import com.igeeksky.xcache.test.book.Book;

import redis.clients.jedis.JedisPoolConfig;

/**
 * @author Tony.Lau
 * @blog: https://my.oschina.net/xcafe
 * @createTime 2017-03-10 03:44:45
 */

public class JedisClusterCacheTest {

	private JedisClusterHandler manager;

	private JedisClusterClient cluster;

	private RedisClusterCache cache;

	private long start;

	private final int length = 1000;

	@Test
	public void testEvit() {
		for (int i = 0; i < length; i++) {
			cache.evict(i);
		}
	}

	@Test
	public void testPut() {
		for (Long i = 0l; i < length; i++) {
			cache.put(i, new Book(i, "aaa", "ddd".getBytes(), 1l));
		}
	}

	@Test
	public void testPutWithVersion() {
		for (Long i = 0l; i < length; i++) {
			int status = cache.putWithVersion(i, new Book(i, "aaa", "ddd".getBytes(), 1l), 2l);
			System.out.println(status);
		}
	}

	@Test
	public void testPutOther() {
		for (Long i = 0l; i < length; i++) {
			String status = cache.putOther(i, 200, "other" + i);
			System.out.println(status);
		}
	}
	
	@Before
	public void before() {
		Set<String> redisClusterNodes = new HashSet<String>();
		redisClusterNodes.add("192.168.0.11:7000");
		redisClusterNodes.add("192.168.0.11:7001");
		redisClusterNodes.add("192.168.0.11:7002");
		JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
		manager = new JedisClusterHandler(redisClusterNodes, jedisPoolConfig);
		cluster = new JedisClusterClient(manager, 3);
		cache = new RedisClusterCache("C_BOOK", 86400, cluster);
		start = System.currentTimeMillis();
	}

	@After
	public void after() {
		System.out.println("执行耗时：" + (System.currentTimeMillis() - start));
	}

}
