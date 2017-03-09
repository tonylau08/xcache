package com.igeeksky.xcache.test.book;

import java.util.List;

public interface BookService {

	Book getBook(Long id);

	Book updateBook(Book book);

	Book createBook(Book book);

	boolean deleteBook(Long id);

	Book getBookByName(String name);

	List<Book> getList(int page, int size);

	<T> T putOther(String key, int seconds, T book);

}
