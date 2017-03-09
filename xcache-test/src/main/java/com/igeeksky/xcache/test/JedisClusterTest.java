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

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.R;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.igeeksky.xcache.extend.redis.RedisClusterScriptManager;
import com.igeeksky.xcache.support.KeyValue;
import com.igeeksky.xcache.support.redis.JedisClusterClient;
import com.igeeksky.xcache.support.redis.JedisClusterHandler;
import com.igeeksky.xcache.support.serializer.GenericJackson2JsonSerializer;
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
	
	public RedisClusterScriptManager scriptManager = new RedisClusterScriptManager("~C_SYSUSER", 86400); 
	
	private long start;
	
	@Test
	public void testBatchDel(){
		int length =10000;
		byte[][] keys = new byte[length][];
		for(int i=0; i< length; i++){
			keys[i] = ("C_BOOK_"+ i).getBytes();
		}
		Long num = cluster.del(keys);
		System.out.println(num);
	}
	
	@Test
	public void testDel(){
		long a = System.currentTimeMillis();
		int length =100000;
		for(int i=0; i< length; i++){
			cluster.del(("C_BOOK_"+ i).getBytes());
		}
		System.out.println(System.currentTimeMillis() - a);
	}
	
	@Test
	public void testSet(){
		long a = System.currentTimeMillis();
		int length =1000;
		for(int i=0; i< length; i++){
			cluster.set(("C_BOOK_"+ i).getBytes(), "".getBytes());
		}
		System.out.println(System.currentTimeMillis() - a);
	}
	
	@Test
	public void testJedisClusterEvalListKey() throws JsonParseException, JsonMappingException, IOException, IllegalArgumentException, ClassNotFoundException{
		//Jackson2JsonRedisSerializer<HashMap> jsonRedisSerializer = new Jackson2JsonRedisSerializer<HashMap>(HashMap.class);
		GenericJackson2JsonSerializer jsonRedisSerializer = new GenericJackson2JsonSerializer();
		ObjectMapper mapper = new ObjectMapper();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE,true);
		
		String key = "~C_SYSUSER";
		int len = 1;
		KeyValue[] kvs = new KeyValue[len];
		for (int i = 0; i < len; i++) {
			String k = key + "~I:"+i;
			KeyValue keyValue = new KeyValue();
			keyValue.setKey(k);
			keyValue.setValue(1);
			keyValue.setFullIdKeyBytes(k.getBytes());;
			kvs[i] = keyValue;
		}
		
		List<Object> list = cluster.evalListKey(new RedisClusterScriptManager(key, 8600).getGetListCmpVerScript(), kvs);
		
		for(int i = 0; i<list.size(); i++){
			Object obj = list.get(i);
			
			@SuppressWarnings("unchecked")
			HashMap<String, R> rs = jsonRedisSerializer.deserialize((byte[])obj, HashMap.class);
			System.out.println(rs.getClass());
			
			Iterator<Entry<String, R>> it = rs.entrySet().iterator();
			while(it.hasNext()){
				Entry<String, R> entry = it.next();
				String id =entry.getKey();
				R r = entry.getValue();
				System.out.println(id);
				
				System.out.println(r);
				if(r.s == 2 || r.s ==4){
					System.out.println(r.o.getClass());
					System.out.println(r.o);
					
					Object ss = jsonRedisSerializer.deserialize(r.o.toString().getBytes());
					System.out.println(ss.getClass());
					System.out.println(ss);
				}
			}
			
		}
	}
	
	/**
	 * <b>DONE<b>
	 */
	@Test
	public void testJedisClusterEvalOneKey(){
		String key = "~C_SYSUSER";
		
		for(Long i=0l; i<10; i++){
			byte[] keyBytes = (key + "~I:"+i).getBytes();
			Book book = new Book(i, "thank", "dddd".getBytes());
			//book.setVersion(2l);
			byte[] bookBytes = jacksonSerializer.serialize(book);
			Object obj = cluster.evalOneKey(scriptManager.getPutCmpVerScript(), keyBytes, bookBytes, String.valueOf(1l).getBytes());
			
			int status = Integer.parseInt(obj.toString());
			System.out.println(status);
			
			byte[] cKeyBytes = (key + "~C").getBytes();
			byte[] idBytes = jacksonSerializer.serialize(i);
			byte[] b = jacksonSerializer.serialize(1);
			if(status == 1){
				cluster.hset(cKeyBytes, idBytes, b);
			}else if(status == 0){
				cluster.hdel(cKeyBytes, idBytes);
			}else{
				//status==2	key集合已经存在，无操作
			}
		}
	}
	
	@Test
	public void testJedisClusterHset(){
		for(Long i=0l; i<1; i++){
			Object obj = cluster.hget("~C_SYSUSER~C".getBytes(), String.valueOf(1).getBytes());
			System.out.println(obj);
		}
	}
	
	@Test
	public void testJedisClusterPut(){
		String key = "~C_SYSUSER";
		
		for(Long i=0l; i<10; i++){
			byte[] keyBytes = (key + "~I:"+i).getBytes();
			Book book = new Book(i, "thank", "dddd".getBytes());
			//book.setVersion(2l);
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
		
		byte[][] keys = new byte[200000][];
		for(int i=0; i<200000;i++){
			keys[i] = ("a"+i).getBytes();
		}
		long s = cluster.del(keys);
		System.out.println(s);
	}
	
	@Test
	public void testJedisClusterSet(){
		
		for(int i=0; i<200000;i++){
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
