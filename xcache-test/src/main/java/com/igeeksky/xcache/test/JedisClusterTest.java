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

import com.igeeksky.xcache.extend.redis.RedisCacheMetadata;
import com.igeeksky.xcache.extend.redis.RedisClusterScriptManager;
import com.igeeksky.xcache.extend.redis.RedisScript;
import com.igeeksky.xcache.support.redis.JedisClusterClient;
import com.igeeksky.xcache.support.redis.JedisClusterHandler;
import com.igeeksky.xcache.support.serializer.GenericJackson2JsonSerializer;
import com.igeeksky.xcache.support.serializer.StringKeySerializer;
import com.igeeksky.xcache.test.book.Book;

import redis.clients.jedis.JedisPoolConfig;

/**
 * @author Tony.Lau
 * @blog: https://my.oschina.net/xcafe
 * @createTime 2017-03-05 03:55:17
 */
public class JedisClusterTest {
	
	private JedisClusterHandler manager;
	
	private JedisClusterClient cluster;
	
	public final GenericJackson2JsonSerializer jacksonSerializer = new GenericJackson2JsonSerializer();
	public final StringKeySerializer keySerializer = new StringKeySerializer();
	
	public RedisClusterScriptManager scriptManager = new RedisClusterScriptManager("C_BOOK", 86400);
	public RedisCacheMetadata metadata = new RedisCacheMetadata("C_BOOK", keySerializer);
	
	private long start;
	
	@Test
	public void testBatchDel(){
		int length =1000;
		byte[][] keys = new byte[length][];
		for(int i=0; i< length; i++){
			keys[i] = metadata.getFullIdKeyBytes(i);
		}
		System.out.println(cluster.del(keys));
	}
	
	@Test
	public void testDel(){
		int length =1000;
		for(int i=0; i< length; i++){
			cluster.del(("~C_BOOK~I:"+ i).getBytes());
		}
	}
	
	@Test
	public void testSet(){
		int length =1000;
		for(int i=0; i< length; i++){
			cluster.set(metadata.getFullIdKeyBytes(i), "".getBytes());
		}
	}
	
	@Test
	public void testJedisClusterEvalOneKey(){
		
		for(Long i=0l; i<10; i++){
			byte[] keyBytes = metadata.getFullIdKeyBytes(i);
			Book book = new Book(i, "thank", "dddd".getBytes(), 2l);
			byte[] bookBytes = jacksonSerializer.serialize(book);
			RedisScript script = scriptManager.getPutCmpVerScript();
			
			Object obj = cluster.evalOneKey(script, keyBytes, bookBytes, String.valueOf(1l).getBytes());
			
			int status = Integer.parseInt(obj.toString());
			System.out.println(status);
			
			byte[] keySetBytes = metadata.getKeySetBytes();
			byte[] idBytes = keySerializer.serialize(i);
			byte[] b = jacksonSerializer.serialize(1);
			if(status == 1){
				cluster.hset(keySetBytes, idBytes, b);
			}else if(status == 0){
				cluster.hdel(keySetBytes, idBytes);
			}else{
				//status==2	key集合已经存在，无操作
			}
		}
	}
	
	@Test
	public void testJedisClusterHset(){
		for(Long i=0l; i<1; i++){
			Object obj = cluster.hget(metadata.getKeySetBytes(), String.valueOf(1).getBytes());
			System.out.println(obj);
		}
	}
	
	@Test
	public void testJedisClusterPut(){
		for(Long i=0l; i<10; i++){
			byte[] keyBytes = metadata.getFullIdKeyBytes(i);
			Book book = new Book(i, "thank", "dddd".getBytes(), 2l);
			byte[] bookBytes = jacksonSerializer.serialize(book);
			Object obj = cluster.set(keyBytes, bookBytes);
			System.out.println(obj);
		}
	}
	
	/**
	 * Redis不支持批量删除位于同一节点的key，仅支持同一slot的key，采用预分片并执行脚本批处理
	 */
	@Test
	public void testJedisClusterDelKeys(){
		
		byte[][] keys = new byte[2000][];
		for(int i=0; i<2000;i++){
			keys[i] = ("a"+i).getBytes();
		}
		long s = cluster.del(keys);
		System.out.println(s);
	}
	
	@Test
	public void testJedisClusterSet(){
		
		for(int i=0; i<2000;i++){
			cluster.set(("a"+i).getBytes(), "ddddd".getBytes());
		}
	}
	
	@Before
	public void before(){
		Set<String> redisClusterNodes = new HashSet<String>();
		redisClusterNodes.add("192.168.0.11:7000");
		redisClusterNodes.add("192.168.0.11:7001");
		redisClusterNodes.add("192.168.0.11:7002");
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
		manager = new JedisClusterHandler(redisClusterNodes, jedisPoolConfig);
		cluster = new JedisClusterClient(manager,3);
		start = System.currentTimeMillis();
	}
	
	@After
	public void after(){
		System.out.println("执行耗时：" + (System.currentTimeMillis() - start));
	}

}
