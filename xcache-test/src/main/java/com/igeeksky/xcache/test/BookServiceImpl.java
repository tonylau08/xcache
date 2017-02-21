package com.igeeksky.xcache.test;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class BookServiceImpl implements BookService {
	
	@Autowired
	private BookDao bookDao;
	
	@Autowired
	private CacheManager cacheManager;
	
	private Cache cache;
	
	@PostConstruct
	public void init(){
		cache = cacheManager.getCache("C_SYS_BOOK_");
	}
	
	@Override
	@Cacheable(value="C_SYS_BOOK_", key="#id.toString()")
	public Book getBook(Long id) {
		return bookDao.getOne(id);
	}

	@Override
	@CacheEvict(value="C_SYS_BOOK_", key="#book.id.toString()")
	public Book updateBook(Book book) {
		return bookDao.updateBook(book);
	}
	
	@Override
	@CachePut(value="C_SYS_BOOK_", key="#book.id.toString()")
	public Book createBook(Book book) {
		return bookDao.createBook(book);
	}
	
	@Override
	@CacheEvict(value="C_SYS_BOOK_", key="#id.toString()")
	public boolean deleteBook(Long id) {
		return bookDao.deleteBook(id);
	}

}
