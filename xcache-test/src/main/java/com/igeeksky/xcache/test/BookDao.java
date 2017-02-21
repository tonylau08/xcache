package com.igeeksky.xcache.test;

public interface BookDao {

	Book getOne(Long id);

	Book updateBook(Book book);

	Book createBook(Book book);

	boolean deleteBook(Long id);

}
