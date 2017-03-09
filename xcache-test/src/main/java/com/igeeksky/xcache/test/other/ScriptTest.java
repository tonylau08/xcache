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

package com.igeeksky.xcache.test.other;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.R;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.igeeksky.dcafe.snowflake.TimeGen;
import com.igeeksky.xcache.extend.redis.RedisScript;
import com.igeeksky.xcache.support.serializer.GenericJackson2JsonSerializer;
import com.igeeksky.xcache.test.book.Book;
import com.igeeksky.xcache.util.BytesUtils;
import com.lambdaworks.redis.RedisURI;
import com.lambdaworks.redis.cluster.RedisAdvancedClusterConnection;
import com.lambdaworks.redis.cluster.RedisClusterClient;
import com.lambdaworks.redis.resource.DefaultClientResources;
import com.lambdaworks.redis.support.RedisClusterClientFactoryBean;

import redis.clients.jedis.Jedis;

/**
 * @author Tony.Lau
 * @blog: https://my.oschina.net/xcafe
 * @createTime 2017-02-21 20:34:29
 */
public class ScriptTest {
	
	public final Jedis jedis = new Jedis("192.168.0.11", 7000);
	
	public final GenericJackson2JsonSerializer jacksonSerializer = new GenericJackson2JsonSerializer();
	
	String evit = "redis.call('HDEL',KEYS[1], ARGV[2]);redis.call('HDEL',KEYS[1], ARGV[2]);";
	
	String clusterPut = "local I_KEY=KEYS[1];local VALUE=ARGV[1];local VERSION=ARGV[2]; if 1==redis.call('EXISTS',I_KEY) then local new_ver = tonumber(VERSION); if type(new_ver) == 'nil' then return 4; end; local old_val = cjson.decode(redis.call('GET',I_KEY)); local old_ver = old_val['version']; if type(old_ver) == 'nil' then return 5; end; if old_ver>new_ver then return 0; elseif old_ver==new_ver then return 3; else redis.call('SETEX',I_KEY, 86400, VALUE); return 2; end; else redis.call('SETEX',I_KEY, 86400, VALUE); return 1; end;";
	
	String get = "local v_field = '~V:'..ARGV[1];local i_field = '~I:'..ARGV[1];local r = {};r['s']=0;if 1==redis.call('HEXISTS',KEYS[1], v_field) then local remote_v = tonumber(redis.call('HGET',KEYS[1], v_field)); local local_v = tonumber(ARGV[2]);if remote_v > local_v then if 1==redis.call('HEXISTS',KEYS[1], i_field) then r['s']=2;r['v']=remote_v;r['o']=redis.call('HGET',KEYS[1], i_field);else r['s']=0;redis.call('HDEL',KEYS[1], v_field);end;elseif remote_v == local_v then r['s']=1;else redis.call('HDEL',KEYS[1], i_field);redis.call('HDEL',KEYS[1], v_field);r['s']=3;end; else redis.call('HDEL',KEYS[1], i_field);r['s']=4;end;return cjson.encode(r);";
	
	String get2 = "if 1==redis.call('HEXISTS',KEYS[1], ARGV[1]) then local local_v = tonumber(ARGV[2]);local obj = redis.call('HGET',KEYS[1], ARGV[1]);local value = cjson.decode(obj);local remote_v = tonumber(value['version']);if remote_v>local_v then return obj;elseif remote_v==local_v then local r={};r['@class']='com.R';r['v']=remote_v;return cjson.encode(r);else redis.call('HDEL',KEYS[1], ARGV[1]);return nil;end;else return nil;end;";
	
	RedisScript clusterPutScript = new RedisScript(clusterPut);
	
	RedisScript getScript = new RedisScript(get2);
	
	@Test
	public void testPut(){
		String key = "~C_SYSUSER";
		byte[] sha1 = clusterPutScript.getShaBytes();
		byte[] script = clusterPutScript.getScriptBytes();
		
		for(Long i=0l; i<100; i++){
			byte[] keyBytes = (key + "~I:"+i).getBytes();
			Book book = new Book(i, "thank", "dddd".getBytes());
			//book.setVersion(2l);
			byte[] bookBytes = jacksonSerializer.serialize(book);
			Object obj = null;
			try{
				obj = jedis.evalsha(sha1, 1, keyBytes, bookBytes, BytesUtils.EMPTY_BYTES);
			}catch(Exception e){
				obj = jedis.eval(script, 1, keyBytes, bookBytes, BytesUtils.EMPTY_BYTES);
			}
			
			System.out.println(obj);
			
			int status = Integer.parseInt(obj.toString());
			byte[] cKeyBytes = (key + "~C").getBytes();
			byte[] idBytes = jacksonSerializer.serialize(i);
			byte[] b = jacksonSerializer.serialize(1);
			if(status == 1){
				jedis.hset(cKeyBytes, idBytes, b);
			}else if(status == 0){
				jedis.hdel(cKeyBytes, idBytes);
			}else{
				//status==2	key集合已经存在，无操作
			}
			
		}
	}
	
	@Test
	public void testClear(){
		String key = "~C_SYSUSER";
		byte[] keyBytes = key.getBytes();
		byte[] eKeyBytes = (key + "~E").getBytes();
		long currSecond = TimeGen.INSTANCE.currSecond()+7200;
		Map<byte[], byte[]> map = jedis.hgetAll(eKeyBytes);
		int size = map.size();
		System.out.println(size);
		Iterator<Entry<byte[], byte[]>> it = map.entrySet().iterator();
		List<byte[]> list = new ArrayList<byte[]>(size);
		while(it.hasNext()){
			Entry<byte[], byte[]> entry = it.next();
			byte[] idBytes = entry.getKey();
			String t = new String(entry.getValue());
			System.out.println(t);
			Long expire = Long.parseLong(t);
			if(expire < currSecond){
				list.add(idBytes);
			}
		}
		byte[][] ids = list.toArray(new byte[list.size()][]);
		System.out.println("ids.length" + ids.length);
		if(ids.length > 0){
			Long s = jedis.hdel(eKeyBytes, ids);
			System.out.println(s);
			Long d = jedis.hdel(keyBytes, ids);
			System.out.println(d);
		}
	}
	
	@Test
	public void testJedisGet(){
		byte[] key = "~C_SYSUSER".getBytes();
		byte[] bytes = jedis.hget(key, "~I:1".getBytes());
		if(null != bytes){
			System.out.println(new String(bytes));
			Object obj = jacksonSerializer.deserialize(bytes);
			if(obj instanceof R){
				System.out.println(R.class.getName());
			}else if(obj instanceof Book){
				System.out.println(Book.class.getName());
			}
			System.out.println(obj);
		}
	}
	
	@Test
	public void testGetScript() throws JsonParseException, JsonMappingException, IOException{
		String key = "~C_SYSUSER";
		byte[] keyBytes = key.getBytes();
		byte[] sha1 = getScript.getShaBytes();
		byte[] script = getScript.getScriptBytes();
		
		byte[] idBytes = "~I:1".getBytes();
		Object obj = null;
		byte[] localV = String.valueOf(1l).getBytes();
		try{
			obj = jedis.evalsha(sha1, 1, keyBytes, idBytes, localV);
		}catch(Exception e){
			System.out.println("get_error");
			obj = jedis.eval(script, 1, keyBytes, idBytes, localV);
		}
		
		if(null != obj){
			if(obj instanceof String){
				System.out.println("String");
			}else if(obj instanceof Long){
				System.out.println("Long");
			}else if(obj instanceof byte[]){
				System.out.println("byte[]");
			}else{
				System.out.println("other");
			}
			System.out.println(new String((byte[]) obj));
			
			Object temp = jacksonSerializer.deserialize((byte[]) obj);
			if(temp instanceof R){
				System.out.println(R.class.getName());
			}else if(temp instanceof Book){
				System.out.println(Book.class.getName());
			}
			System.out.println(temp);
		}
		jedis.close();
		
	}
	
	@Test
	public void testLetture() throws Exception{
		RedisClusterClientFactoryBean factory = new RedisClusterClientFactoryBean();
		
		DefaultClientResources re = new DefaultClientResources.Builder().build();
		RedisURI uri1 = new RedisURI("192.168.0.11", 7000, 100, TimeUnit.SECONDS);
		RedisURI uri2 = new RedisURI("192.168.0.11", 7000, 100, TimeUnit.SECONDS);
		RedisURI uri3 = new RedisURI("192.168.0.11", 7000, 100, TimeUnit.SECONDS);
		List<RedisURI> redisURIs = new ArrayList<RedisURI>();
		redisURIs.add(uri1);
		redisURIs.add(uri2);
		redisURIs.add(uri3);
		
		factory.setClientResources(re);
		factory.setRedisURI(uri1);
		
		RedisClusterClient cli = RedisClusterClient.create(redisURIs);
		cli.setDefaultTimeout(100, TimeUnit.SECONDS);
		RedisAdvancedClusterConnection<String, String> s = cli.connectCluster();
		System.out.println(s);
	}
	
}
