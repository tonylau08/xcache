package com.igeeksky.xcache.test.book;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.igeeksky.xcache.core.CacheListResult;
import com.igeeksky.xcache.core.Xcache;

@Service
public class BookServiceImpl implements BookService {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private BookDao bookDao;
	
	private Xcache xcache;
	
	@Autowired
	private CacheManager cacheManager;
	
	@PostConstruct
	public void construct(){
		xcache = (Xcache) cacheManager.getCache("~C_BOOK");
	}
/*
	@Autowired
	private CacheHelper cacheHelper;
	*/
	@Override
	public <T>T putOther(String key, int seconds, T book){
		xcache.putOther(key, seconds, book);
		
		@SuppressWarnings("unchecked")
		T value = (T) xcache.getOther(key, book.getClass());
		return value;
	}
	
	

	@Override
	@Cacheable(value="~C_BOOK", key="#id")
	public Book getBook(Long id) {
		Book book = bookDao.getOne(id);
		if (null != book)  xcache.putRelation(book.getName(), book, "id");
		return book;
	}

	@Override
	public Book getBookByName(String name) {
		Book book = xcache.getRelation(name, Book.class);
		if(null != book){
			logger.info("getBookByName : book is not null"+ name);
		}
		if(null == book){
			book = bookDao.getByName(name);
			if(null != book){
				xcache.putRelation(name, book, "id");
			}
		}
		return book;
	}

	@Override
	@CacheEvict(value = "~C_BOOK", key = "#book.id")
	public Book updateBook(Book book) {
		return bookDao.updateBook(book);
	}

	@Override
	@CachePut(value = "~C_BOOK", key = "#book.id")
	public Book createBook(Book book) {
		return bookDao.createBook(book);
	}

	@Override
	@CacheEvict(value = "~C_BOOK", key = "#id", condition="#result==false")
	public boolean deleteBook(Long id) {
		return bookDao.deleteBook(id);
	}
	
	public List<Book> getList(int page, int size){
		long offset = (page - 1l) * size;
		CacheListResult<Long, Book> cacheList = xcache.getList("getList_" + offset+ "_" +size, Long.class, Book.class);
		List<Book> results = null;
		if(null == cacheList){
			logger.info("cacheList is null");
			results = bookDao.getList(offset, size);
			if(null != results){
				xcache.putList("getList_" + offset+ "_" +size, results.toArray(new Book[results.size()]), "id", Long.class);
			}
			return results;
		}
		
		List<Long> ids = cacheList.getAllIds();
		logger.info("cacheList is not null" + ids.toString());
		List<Long> nonCacheIds = cacheList.getNonCacheIds();
		Map<Long, Book> map = cacheList.getCacheElements();
		Map<Long, Book> nonCache = null;
		int nonCacheSize = nonCacheIds.size();
		
		if(nonCacheSize>0){
			nonCache = bookDao.getByIds(nonCacheIds);
			logger.info("nonCache"+nonCache.size());
		}
		results = new ArrayList<Book>();
		
		for(Long id : ids){
			Book book = map.get(id);
			if(null == book && nonCacheSize > 0){
				book = nonCache.get(id);
			}
			if(null == book){
				book = bookDao.getOne(id);
				logger.info("getList error id : " + id);
			}
			if(null != book){
				results.add(book);
			}
		}
		return results;
	}

}
