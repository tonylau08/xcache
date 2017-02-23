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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisKeyValueTemplate;
import org.springframework.data.redis.core.RedisTemplate;import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Tony.Lau
 * @blog: https://my.oschina.net/xcafe
 * @createTime 2017-02-21 20:34:29
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-context.xml")
public class CacheTest {
	
	//@Autowired
	//private BookService service;
	
	@Autowired
	private RedisTemplate<String, Object> redisTemplate;
	
	String put = "local version = redis.call('HINCRBY',KEYS[2], ARGV[2], 1);"
			+ "if version > 0 then "
			+ "redis.call('SETEX',KEYS[1], 3600, ARGV[1]);"
			+ "return version;else "
			+ "redis.call('HDEL', KEYS[2], id);"
			+ "redis.call('DEL', KEYS[1]);"
			+ "return 0;end;";
	
	String get = "local local_ver = ARGV[2];" 
			+ "local version = redis.call('HGET',KEYS[2], ARGV[1]);"
			+ "if version <= 0 then "
			+ "return nil;end;"
			+ "if local_ver < version then "
			+ "local value = redis.call('GET',KEYS[1]);"
			+ "return version;end;"
			+ "else "
			+ "redis.call('HDEL', KEYS[2], id);"
			+ "redis.call('DEL', KEYS[1]);"
			+ "return 0;end;";
	
	String get2 = "local remote_v = redis.call('HGET',KEYS[1], ARGV[1]);"
			+ "local result; local local_v = tonumber(ARGV[2])"
			+ "remote_v = tonumber(remote_v);"
			+ "if remote_v == local_v then result = true;else result=-1; end;" 
			/*+ "if remote_v then "
					+ "remote_v = tonumber(remote_v);"
					+ "if remote_v == local_v then "
						+ "result = redis.call('GET',KEYS[2]);"
					+ "else result='remote_v:'+remote_v;end;"
			+ " else redis.call('DEL',KEYS[2]);result = 'remote_v:null';end;"*/
			+ "return -99,-33;";
	
			/**
			 1.获取远程版本
			 2.如果远程版本存在
			 	2.1 如果远程版本大于本地版本获取远程结果
			 		2.1.1 如果结果不为空，返回远程结果
			 		2.1.2 如果结果为空，返回"result:null"
			 	2.2 如果远程版本小于等于本地版本，返回远程版本"remote_v:equal:"+remote_v
			 
			 4.如果远程版本不存在，删除key，返回"romote_v:null"
			 */
	String evit = "redis.call('DEL',KEYS[1]);"
			+ "redis.call('HDEL',KEYS[2], ARGV[1]);";
	
	String clear = "redis.call('DEL',KEYS[1]);批量删除key"
			+ "redis.call('DEL',KEYS[2]);";
			/*
			+ "if version > ARGV[2] then"
			+ "local result = redis.call('GET',KEYS[2]);end;else "
			+ "redis.call('HDEL',KEYS[2]);return version;end;"
			+ "else "
			+ "";*/
	
	DefaultRedisScript<Long> putScript = new DefaultRedisScript<>(put, Long.class);
	
	DefaultRedisScript<Object> getScript = new DefaultRedisScript<>(get2, Object.class);
	
	@Test
	public void testGetScript(){
		/**
		KEYS[1]: C_SYS_USER_VER
		KEYS[2]: C_SYS_USER:id
		
		ARGV[1]: id
		ARGV[2]: localVersion
		*/
		
		/**
		local local_ver = tonumber(ARGV[2]);
		local version = redis.call('HGET',KEYS[1], ARGV[1]);
		if version <= 0 then 
			result = ;end;
		if local_ver >= version then 
			return local_ver;end;
		
		
		local value = redis.call('GET',KEYS[1]);
		return version;end;
		 else 
		redis.call('HDEL', KEYS[2], id);
		redis.call('DEL', KEYS[1]);
		return 0;end;
		*/
		
		List<String> keys = new ArrayList<String>();
		String id = new Long(2l).toString();
		keys.add("C_SYS_USER_VER");
		keys.add("C_SYS_USER:2"+id);
		Object version = redisTemplate.execute(getScript, keys, id, 3);
		System.out.println(version);
	}
	
	@Test
	public void testPutScript(){
		
		/**
		KEYS[1]: C_SYS_USER:id
		KEYS[2]: C_SYS_USER_VER
		
		ARGV[1]: value
		ARGV[2]: id
		*/
		
		long start = System.currentTimeMillis();
		for(Long i=0l; i<100; i++){
			List<String> keys = new ArrayList<String>();
			String id = i.toString();
			keys.add("C_SYS_USER:" + id);
			keys.add("C_SYS_USER_VER");
			Book book = new Book(i, "my_love");
			Long version = redisTemplate.execute(putScript, keys, book, id);
		}
		
		System.out.println("耗时：" + (System.currentTimeMillis() - start));
	}
	
	@Test
	public void testPipeline(){
		long start = System.currentTimeMillis();
		
		for(Long i=0l; i<10000; i++){
			String id = i.toString();
			Book book = new Book(i, "my_love");
			redisTemplate.opsForValue().set("C_SYS_USER:" + id, book, 3600, TimeUnit.SECONDS);
			redisTemplate.opsForHash().increment("C_SYS_USER_VER", id, 1);
		}
		
		System.out.println("耗时：" + (System.currentTimeMillis() - start));
		
	}
	/*
	@Test
	public void testCreate(){
		long curr = System.currentTimeMillis();
		for(Long i=3l; i<20000; i++){
			Book book = new Book(i, "name_"+i);
			book = service.createBook(book);
		}
		
		long two = System.currentTimeMillis();
		System.out.println(two - curr);
		
		for(Long i=0l; i<20000; i++){
			Book book = service.getBook(i);
			if(i%100==0){
				System.out.println(book);
			}
		}
		
		System.out.println(System.currentTimeMillis()-two);
	}
	
	@Test
	public void testGet(){
		long curr = System.currentTimeMillis();
		for(Long i=0l; i<20000; i++){
			Book book = service.getBook(i);
			if(i%100==0){
				System.out.println(book);
			}
		}
		System.out.println(System.currentTimeMillis()-curr);
	}
	
	@Test
	public void testUpdate(){
		for(Long i=1l; i<200; i++){
			Book book = new Book(i, "xname_"+i);
			book = service.updateBook(book);
			System.out.println(book);
		}
	}
	
	@Test
	public void testDelete(){
		for(Long i=1l; i<20000; i++){
			System.out.println(service.deleteBook(i));
		}
	}
*/
}
