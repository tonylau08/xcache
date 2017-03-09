package com.igeeksky.xcache.test.book;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class BookDaoImpl implements BookDao {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final ConcurrentHashMap<Long, Book> map = new ConcurrentHashMap<Long, Book>();
	
	@PostConstruct
	public void init(){
		map.put(1l, new Book(1l , "name_1", "dddd".getBytes(), 1l));
		map.put(2l, new Book(2l , "sssdacfe", "cccc".getBytes(), 1l));
	}

	@Override
	public Book getOne(Long id) {
		Book book = map.get(id);
		logger.info("getOne:" + book);
		return book;
	}

	@Override
	public Book updateBook(Book book) {
		logger.info("updateBook:" + book);
		if(map.get(book.getId()) != null){
			map.put(book.getId(), book);
			return book;
		}
		return null;
	}
	
	@Override
	public boolean deleteBook(Long id) {
		logger.info("deleteBook:" + map.get(id));
		map.remove(id);
		return true;
	}
	
	@Override
	public Book createBook(Book book) {
		if(map.get(book.getId()) == null){
			logger.info("createBook:" + book);
			map.put(book.getId(), book);
			return book;
		}
		return book;
	}

	@Override
	public Long getIdByName(String name) {
		if(StringUtils.isEmpty(name)){
			return null;
		}
		Set<Long> set = map.keySet();
		for(Long id : set){
			Book book = map.get(id);
			if(book != null && name.equals(book.getName())){
				return id;
			}
		}
		return null;
	}

	@Override
	public List<Book> getList(long offset, int size) {
		logger.info("bookDao getList:" + offset + "_" + size);
		List<Book> list = new ArrayList<Book>();
		for(Long i=offset; i< offset+size; i++){
			Book book = map.get(i);
			if(null != book){
				list.add(book);
			}
		}
		return list;
	}

	@Override
	public Book getByName(String name) {
		if(StringUtils.isEmpty(name)){
			return null;
		}
		Set<Long> set = map.keySet();
		for(Long id : set){
			Book book = map.get(id);
			if(book != null && name.equals(book.getName())){
				return book;
			}
		}
		return null;
	}

	@Override
	public Map<Long, Book> getByIds(List<Long> nonCacheIds) {
		logger.info("getByIds: "+ nonCacheIds.toString());
		if(null != nonCacheIds && nonCacheIds.size() >0){
			Map<Long, Book> map = new HashMap<Long, Book>();
			for(Long id : nonCacheIds){
				Book book = map.get(id);
				if(null != book){
					map.put(id, book);
				}
			}
			return map;
		}
		return Collections.emptyMap();
	}

}
