package com.igeeksky.xcache.test;

import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

@Service
public class BookDaoImpl implements BookDao {
	
	private final ConcurrentHashMap<Long, Book> map = new ConcurrentHashMap<Long, Book>();
	
	@PostConstruct
	public void init(){
		map.put(1l, new Book(1l , "xcache"));
		map.put(2l, new Book(2l , "dacfe"));
	}

	@Override
	public Book getOne(Long id) {
		Book book = map.get(id);
		/*if(null != book){
			System.out.println("getOne:" + book.toString());
		}*/
		return book;
	}

	@Override
	public Book updateBook(Book book) {
		//System.out.println("updateBook:" + book.toString());
		if(map.get(book.getId()) != null){
			map.put(book.getId(), book);
			return book;
		}
		return null;
	}
	
	@Override
	public boolean deleteBook(Long id) {
		//System.out.println("deleteBook:" + map.get(id).toString());
		map.remove(id);
		return true;
	}
	
	@Override
	public Book createBook(Book book) {
		if(map.get(book.getId()) == null){
			//System.out.println("createBook:" + book.toString());
			map.put(book.getId(), book);
			return book;
		}
		return book;
	}

}
