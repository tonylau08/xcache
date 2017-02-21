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
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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
	
	@Autowired
	private BookService service;
	
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
		for(Long i=1l; i<20000; i++){
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

}
