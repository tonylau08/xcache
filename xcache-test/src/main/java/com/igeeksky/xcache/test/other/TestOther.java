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

import java.beans.IntrospectionException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nustaq.serialization.FSTConfiguration;
import org.springframework.util.Assert;

import com.KV;
import com.R;
import com.igeeksky.xcache.extend.redis.RedisCacheMetadata;
import com.igeeksky.xcache.support.redis.JedisClusterHandler;
import com.igeeksky.xcache.support.serializer.FSTSerializer;
import com.igeeksky.xcache.support.serializer.GenericJackson2JsonSerializer;
import com.igeeksky.xcache.support.serializer.Jackson2JsonSerializer;
import com.igeeksky.xcache.support.serializer.KeySerializer;
import com.igeeksky.xcache.support.serializer.StringKeySerializer;
import com.igeeksky.xcache.test.book.Book;
import com.igeeksky.xcache.util.BeanUtils;
import com.igeeksky.xcache.util.BytesUtils;
import com.igeeksky.xcache.util.NumUtils;


/**
 * @author Tony.Lau
 * @blog: https://my.oschina.net/xcafe
 * @createTime 2017-02-23 21:56:35
 */
public class TestOther {
	
	private long start;
	
	@Before
	public void before(){
		start = System.currentTimeMillis();
	}
	
	@After
	public void after(){
		System.out.println("耗时：" + (System.currentTimeMillis() - start));
	}
	
	public final String[] array = new String[2];
	
	@Test
	public void testHashSetToArray(){
		Set<String> set = new HashSet<String>();
		set.add("a");
		set.add("b");
		set.add("c");
		set.add("d");
		String[] ss = new String[7];
		set.toArray(ss);
		ss[7-3]="e";
		ss[7-2]="f";
		ss[7-1]="g";
		for(String s : ss){
			System.out.println(s);
		}
		
	}
	
	@Test
	public void testRandom(){
		Random random = new Random();
		int x__0 = 0;
		int x0_5000 = 0;
		int x5001_10000 = 0;
		int x10001_16383 = 0;
		
		for(int i=0; i<100; i++){
			int x = random.nextInt(JedisClusterHandler.CLUSTER_SLOT_MAXNUM);
			if(x < 0){
				x__0 ++;
			}else if(x>=0 && x <= 5000){
				x0_5000++;
			}else if(x>5000 && x <= 10000){
				x5001_10000++;
			}else{
				x10001_16383++;
			}
		}
		System.out.println("x__0: " + x__0);
		System.out.println("x0_5000: " + x0_5000);
		System.out.println("x5001_10000: " + x5001_10000);
		System.out.println("x10001_16383: " + x10001_16383);
	}
	
	@Test
	public void testArrayLock() throws InterruptedException{
		Thread t1 = new Thread(new Runnable(){
			@Override
			public void run() {
				for(int i = 0; i<200000000; i++){
					array[i%2] = "dddd" + i + "dddd";
				}
			}
		});
		Thread t2 = new Thread(new Runnable(){
			@Override
			public void run() {
				for(int i = 0; i<200000000; i++){
					array[i%2] = "ssss" + i + "ssss";
				}
			}
		});
		ExecutorService executor = Executors.newFixedThreadPool(8);
		executor.submit(t1);
		executor.submit(t2);
		executor.submit(t1);
		executor.submit(t2);
		executor.submit(t1);
		executor.submit(t2);
		executor.submit(t1);
		executor.submit(t2);
		executor.submit(t1);
		executor.submit(t2);
		Thread.sleep(20000);
		
		for(String s : array){
			System.out.println(s);
		}
		
	}
	
	@Test
	public void testJsonArray(){
		FSTSerializer json = new FSTSerializer();
		
		String a = "2222l";
		String b = "22222";
		Long c = 2222l;
		Integer d = 2222;
		Object[] array = new Object[]{a, b, c, d};
		byte[] arrays = json.serialize(array);
		Object[] aaa = (Object[])json.deserialize(arrays);
		for(Object o : aaa){
			System.out.println();
			System.out.println(o);
			System.out.println(o.getClass());
		}
	}
	
	@Test
	public void testJsonObject(){
		//GenericJackson2JsonSerializer json = new GenericJackson2JsonSerializer();
		FSTSerializer json = new FSTSerializer();
		
		String a = "2222l";
		String b = "22222";
		Long c = 2222l;
		Integer d = 2222;
		byte[] abytes = json.serialize(a);
		byte[] bbytes = json.serialize(b);
		byte[] cbytes = json.serialize(c);
		byte[] dbytes = json.serialize(d);
		
		Object ao = json.deserialize(abytes);
		Object bo = json.deserialize(bbytes);
		Object co = json.deserialize(cbytes);
		Object doo = json.deserialize(dbytes);
		
		System.out.println("ao");
		System.out.println(ao);
		System.out.println(abytes.length);
		System.out.println(ao.getClass());
		
		System.out.println("\nbo");
		System.out.println(bo);
		System.out.println(bbytes.length);
		System.out.println(bo.getClass());
		
		System.out.println("\nco");
		System.out.println(co);
		System.out.println(cbytes.length);
		System.out.println(co.getClass());
		
		System.out.println("\ndoo");
		System.out.println(doo);
		System.out.println(dbytes.length);
		System.out.println(doo.getClass());
		
		
		/*
		byte[] jbytes = json.serialize(a);
		byte[] sbytes = a.toString().getBytes();
		
		byte[] mergej = BytesUtils.merge(pbytes, jbytes);
		System.out.println(new String(mergej));
		
		byte[] merges = BytesUtils.merge(pbytes, sbytes);
		System.out.println(new String(merges));
		
		
		
		System.out.println("jackson:		"+jbytes.length);
		System.out.println("String:			"+sbytes.length);
		
		Object s = json.deserialize(mergej);
		System.out.println(s);
		System.out.println(s.toString().getBytes().length);
		System.out.println("jackson:		"+s.getClass());
		Object x = json.deserialize(merges);
		System.out.println("String:			"+x.getClass());
		System.out.println(x);*/
	}
	
	@Test
	public void testBytesString2(){
		Integer a = 20000;
		GenericJackson2JsonSerializer json = new GenericJackson2JsonSerializer();
		for(int i=0;i<100000;i++){
			byte[] jbytes = json.serialize(a);
		}
	}
	
	@Test
	public void testFstSer(){
		Book book = new Book(1l, "dddd", "dsdfs".getBytes());
		FSTConfiguration configuration = FSTConfiguration.createDefaultConfiguration();
		byte[] bookBytes = configuration.asByteArray(book);
		
		KeySerializer< Object, Object> keySerializer = new StringKeySerializer();
		String nullstr = null;
		byte[] s = keySerializer.serialize(nullstr);
		System.out.println("nullstr" + s.length);
		
		
		String d = "dddd";
		byte[] dd = "d".getBytes();
		
		Object o = keySerializer.deserialize(dd);
		System.out.println(o);
		
		
		
		RedisCacheMetadata metadata = new RedisCacheMetadata("C_BOOK", keySerializer);
		byte[] keyBytes = metadata.getFullIdKeyBytes(book);
		
		System.out.println(bookBytes.length);
		System.out.println(keyBytes.length);
		System.out.println(new String(bookBytes));
		System.out.println(new String(keyBytes));
		byte[] idBytes = metadata.getSuffixIdBytes(keyBytes);
		System.out.println(idBytes.length);
		
		Object a = keySerializer.deserialize(idBytes);
		Object b = keySerializer.deserialize(bookBytes);
		System.out.println(a);
		System.out.println(b);
		
	}
	
	@Test
	public void testStringSer(){
		Long s = -2222l;
		FSTConfiguration configuration = FSTConfiguration.createDefaultConfiguration();
		byte[] sb = configuration.asByteArray(s);
		System.out.println(new String(sb));
		
		Long d = -3333l;
		byte[] db = configuration.asByteArray(d);
		System.out.println(new String(db));
		
		
		Integer g = -3333;
		byte[] gb = configuration.asByteArray(g);
		System.out.println(new String(gb));
		
		Object so = configuration.asObject(sb);
		System.out.println(so.getClass());
		System.out.println(so);
		
		
		String a = "-2222";
		System.out.println(so.equals(a));
		byte[] ab = configuration.asByteArray(a);
		System.out.println(ab.length);
		Object ao = configuration.asObject(ab);
		System.out.println(ao.getClass());
		System.out.println(new String(ab));
		
		
		byte[] abb = a.getBytes();
		System.out.println(abb.length);
		
		/*System.out.println(n.getClass());
		if(n instanceof Number){
			System.out.println(true);
		}
		System.out.println(n);*/
		//System.out.println(n.intValue() == 111);
	}
	
	
	@Test
	public void testBeanUtil(){
		Book book = new Book(2l, "dd", "".getBytes());
		book.setId(null);
		for(int i=0; i<100000000; i++){
			//Object obj = BeanUtils.getBeanProperty(book, "id");
			Object o = BeanUtils.getBeanProperty(book, "id");
		}
	}
	
	@Test
	public void testLinkedList(){
		List<Integer> list = new LinkedList<Integer>();
		list.add(null);
		list.add(1);
		list.add(2);
		System.out.println(list.size());
		System.out.println(list.get(0));
		System.out.println(list.get(1));
		System.out.println(list.get(2));
	}
	
	@Test
	public void testSer(){
		Jackson2JsonSerializer<Long> Jackson2JsonSerializer = new Jackson2JsonSerializer<Long>(Long.class);
		FSTSerializer fstSerializer = new FSTSerializer();
		
		Integer a = 1;
		
		Long b = Long.MAX_VALUE;
		
		byte[] aByte = fstSerializer.serialize(a);
		
		byte[] bByte = fstSerializer.serialize( b);
		
		System.out.println(aByte.length);
		System.out.println(bByte.length);
		
		System.out.println(aByte.equals(bByte));
		
		byte[] aStr = a.toString().getBytes();
		
		byte[] bStr = b.toString().getBytes();
		
		
		System.out.println(aStr.equals(bStr));
		
		System.out.println(aStr.length);
		System.out.println(bStr.length);
		
		Long c = (Long)fstSerializer.deserialize(bByte);
		System.out.println(c);
	}
	
	@Test
	public void testJson(){
		Jackson2JsonSerializer<Book> jsonRedisSerializer = new Jackson2JsonSerializer<Book>(Book.class);
		FSTSerializer fstSerializer = new FSTSerializer();
		
		Book book = new Book(1l, "很好", "dddddd".getBytes());
		
		for(int i=0; i<1; i++){
			byte[] f = fstSerializer.serialize(book);
			System.out.println(f.length);
			System.out.println(new String(f));
			fstSerializer.deserialize(f);
			
			byte[] j = jsonRedisSerializer.serialize(book);
			System.out.println(j.length );
			System.out.println(new String(j));
			jsonRedisSerializer.deserialize(j);
		}
	}
	
	@Test
	public void testJson6(){
		GenericJackson2JsonSerializer jsonRedisSerializer = new GenericJackson2JsonSerializer();
		Jackson2JsonSerializer<KV[]> serializer = new Jackson2JsonSerializer<KV[]>(KV[].class);
		Long a = 1111l;
		byte[] al = a.toString().getBytes();
		System.out.println(al.length);
		System.out.println(new String(al));
		
		
		byte[] bl = jsonRedisSerializer.serialize(a);
		System.out.println(bl.length);
		System.out.println(new String(bl));
	}

	
	@Test
	public void testJson5(){
		GenericJackson2JsonSerializer jsonRedisSerializer = new GenericJackson2JsonSerializer();
		Jackson2JsonSerializer<KV[]> serializer = new Jackson2JsonSerializer<KV[]>(KV[].class);
		KV[] kvs = new KV[3];
		/*kvs[0] = new KV("aaaa".getBytes(),String.valueOf(111l).getBytes());
		kvs[1] = new KV("bbbb".getBytes(),String.valueOf(222l).getBytes());
		kvs[2] = new KV("cccc".getBytes(),String.valueOf(333l).getBytes());*/
		byte[] a = jsonRedisSerializer.serialize(kvs);
		System.out.println(new String(a));
		byte[] b = serializer.serialize(kvs);
		System.out.println(new String(b));
		
		KV[] oa = (KV[])jsonRedisSerializer.deserialize(a);
		System.out.println("oa:" + oa.getClass());
		System.out.println(new String((byte[])oa[0].getK()));
		
		KV[] ob = serializer.deserialize(b);
		System.out.println("ob" + ob.getClass());
	}
	
	@Test
	public void testJson4(){
		GenericJackson2JsonSerializer jsonRedisSerializer = new GenericJackson2JsonSerializer();
		
		Jackson2JsonSerializer<R> serializer = new Jackson2JsonSerializer<R>(R.class);
		
		
		
		R result = new R();
		Book book = new Book(1l, "很好", "dddddd".getBytes());
		result.s=1;
		result.v=2l;
		result.o=book;
		for(int i=0; i<1; i++){
			byte[] j = jsonRedisSerializer.serialize(result);
			System.out.println(new String(j));
			System.out.println(j.length );
			R temp = (R)jsonRedisSerializer.deserialize(j);
			System.out.println(temp.o.getClass());
			System.out.println(temp.o);
		}
	}
	
	@Test
	public void testJson2(){
		//GenericJackson2JsonSerializer jsonRedisSerializer = new GenericJackson2JsonSerializer();
		Jackson2JsonSerializer<String> jsonRedisSerializer = new Jackson2JsonSerializer<String>(String.class);
		String s = "aaaabbbbb";
		byte[] sb =s.getBytes();
		System.out.println(sb.length);
		System.out.println(new String(sb));
		byte[] sj = jsonRedisSerializer.serialize(s);
		System.out.println(sj.length);
		System.out.println(new String(sj));
	}
	
	@Test
	public void testJson3(){
		GenericJackson2JsonSerializer jsonRedisSerializer = new GenericJackson2JsonSerializer();
		for(int i=0; i<1; i++){
			byte[] j = jsonRedisSerializer.serialize(11111111l);
			System.out.println(new String(j));
			System.out.println(j.length );
		}
		
	}
	
	@Test
	public void testVersion() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, IntrospectionException{
		Long version = null;
		for(int i=0; i<100000000; i++){
			Book book = new Book(1l,"ddd", "ddd".getBytes());
			book.setVersion(1111111111l);
			version = versionGen(book);
		}
		System.out.println(version);
	}
	
	public Long versionGen(Object value) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, IntrospectionException, InvocationTargetException{
		Assert.notNull(value, "value must not be null");
		Field field = value.getClass().getDeclaredField("version");
		field.setAccessible(true);
		return NumUtils.getLong(field.get(value), field.getType());
	}
	
	@Test
	public void testString(){
		String a = "abcdefg";
		String b = "hijk";
		byte[] bytesA = a.getBytes();
		byte[] bytesB = b.getBytes();
		
		for(int i=0; i<1; i++){
			String c = new String(BytesUtils.merge(bytesA,bytesB ));
			System.out.println(c);
		}
	}
	
	
	
	@Test
	public void testString2(){
		String a = "abcdefg";
		String b = "hijk";
		
		for(int i=0; i<100000000; i++){
			String c = a+b;
			byte[] d = c.getBytes();
		}
	}

}
