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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * @author Tony.Lau
 * @blog: https://my.oschina.net/xcafe
 * @createTime 2017-02-26 01:36:30
 */
@Service
public class CacheHelperImpl implements CacheHelper {
	
	@Autowired
	private BookDao bookDao;
	
	@Override
	@Cacheable(value="~C_BOOK", key="'~O:'+#name")
	public Long getId(String name){
		return bookDao.getIdByName(name);
	}

	@Override
	public Book getById(Long id) {
		return bookDao.getOne(id);
	}
	
	@Override
	@CachePut(value="~C_BOOK", key="'~O:'+#name")
	public Long put(String name, long id){
		return id;
	}

	@Override
	public Long[] getIds(long offset, int size) {
		List<Book> list = bookDao.getList(offset, size);
		return putIds(offset, size, list);
	}

	@Override
	@Cacheable(value="~C_BOOK", key="'~L:' + #offset + '~'#size")
	public Long[] putIds(long offset, int size, List<Book> list) {
		int listSize = list.size();
		Long[] ids = new Long[listSize];
		for(int i=0; i<listSize; i++){
			Book book = list.get(i);
			ids[i] = book.getId();
		}
		return ids;
	}

}
