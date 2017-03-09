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

package com.igeeksky.xcache.test.book;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Tony.Lau
 * @blog: https://my.oschina.net/xcafe
 * @createTime 2017-03-09 18:52:04
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-context.xml")
public class BookCacheTest {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private BookService service;
	
	private long start;
	
	@Before
	public void before(){
		start = System.currentTimeMillis();
	}
	
	@After
	public void after(){
		logger.info("耗时：" + (System.currentTimeMillis() - start));
	}
	
	@Test
	public void testAll(){
		testCreate();
		testPutRelation();
		//testUpdate();
		//testGet();
		//testGetList();
		//testDelete();
	}
	
	@Test
	public void testPutRelation(){
		for(Long i=0l; i<10; i++){
			Book book = service.getBookByName("name_"+i);
			logger.info("testPutRelation" + book);
		}
		for(Long i=0l; i<10; i++){
			Book book = service.getBookByName("name_"+i);
			logger.info("testPutRelation" + book);
		}
	}
	
	@Test
	public void testPutOther(){
		String key = "eeeeeeeeeeeeeee";
		Book value = service.putOther(key, 3600, new Book(1l, "dddd", "xxxx".getBytes(), 1l));
		logger.info(value.toString());
	}
	
	@Test
	public void testGetList(){
		List<Book> list = service.getList(2, 5);
		for(Book book : list){
			logger.info(book.toString());
		}
		
		
		list = service.getList(2, 5);
		for(Book book : list){
			logger.info(book.toString());
		}
	}
	
	@Test
	public void testCreate(){
		for(Long i=0l; i<10; i++){
			Book book = new Book(i, "name_"+i, "ddd".getBytes());
			book.setVersion(1l);
			book = service.createBook(book);
			if(i%100==0){
				logger.info("testCreate: " + book);
			}
		}
	}
	
	@Test
	public void testGet(){
		for(Long i=0l; i<5; i++){
			Book book = service.getBook(i);
			if(i%100==0){
				logger.info("testGet: " + book);
			}
		}
	}
	
	@Test
	public void testUpdate(){
		for(Long i=1l; i<10; i++){
			Book book = new Book(i, "name_"+i, "ddd".getBytes());
			book.setVersion(2l);
			book = service.updateBook(book);
			if(i%5==0){
				logger.info("testUpdate: "+ book);
			}
		}
	}
	
	
	@Test
	public void testDelete(){
		Boolean bool = service.deleteBook(4l);
		logger.info(bool.toString());
		/*for(Long i=1l; i<2; i++){
			boolean bool = service.deleteBook(3l);
			if(i%5==0){
				logger.info("testDelete: "+ bool);
			}
		}*/
	}

}
