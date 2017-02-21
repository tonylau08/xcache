package com.igeeksky.xcache.test;


public interface BookService {
	
	Book getBook(Long id);

	Book updateBook(Book book);

	Book createBook(Book book);

	boolean deleteBook(Long id);

}
