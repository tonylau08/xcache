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

import org.junit.Test;

import com.igeeksky.xcache.extend.redis.RedisScript;
import com.igeeksky.xcache.support.redis.JedisClient;
import com.igeeksky.xcache.support.serializer.GenericJackson2JsonSerializer;
import com.igeeksky.xcache.test.book.Book;
import com.igeeksky.xcache.util.BytesUtils;

import redis.clients.jedis.JedisPoolConfig;

/**
 * @author Tony.Lau
 * @blog: https://my.oschina.net/xcafe
 * @createTime 2017-03-09 02:15:33
 */
public class JedisSingleTest {
	
	String clear = "local KEY_SET = KEYS[1]; local LIST_KEY=KEYS[2]; local REL_KEY=KEYS[3]; local PREFIX=ARGV[1]; local del_num=0; if 1==redis.call('EXISTS',KEY_SET) then local all_key = redis.call('HKEYS',KEY_SET); if type(all_key)=='table' then for i in ipairs(all_key) do local full_Key = PREFIX..all_key[i]; del_num = del_num + redis.call('DEL', full_Key); end; end; end; redis.call('DEL', KEY_SET);redis.call('DEL', LIST_KEY);redis.call('DEL', REL_KEY); return del_num;";
	
	RedisScript clearScript = new RedisScript(clear);
	
	JedisClient jedisClient = new JedisClient("127.0.0.1:6379", new JedisPoolConfig());
	
	GenericJackson2JsonSerializer ValueSerializer = new GenericJackson2JsonSerializer();
	
	@Test
	public void testSingleClear(){
		String prefix = "C_BOOK~I:";
		String keySet = "C_BOOK~KEYSET";
		Object obj = jedisClient.eval(clearScript, 1, keySet.getBytes(), prefix.getBytes());
		System.out.println(obj);
	}
	
	@Test
	public void testSingleSet(){
		String prefix = "C_BOOK~I:";
		String keySet = "C_BOOK~KEYSET";
		for(int i=0; i<100; i++){
			Book book = new Book(Long.getLong(""+i), "aaaa", "dddd".getBytes(), 1l);
			jedisClient.setex((prefix+i).getBytes(), 3600, ValueSerializer.serialize(book));
			jedisClient.hset(keySet.getBytes(), (""+i).getBytes(), BytesUtils.ONE_BYTES);
		}
		
	}

}
