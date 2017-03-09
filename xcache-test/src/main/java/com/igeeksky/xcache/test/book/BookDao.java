package com.igeeksky.xcache.test.book;

import java.util.List;
import java.util.Map;

public interface BookDao {

	Book getOne(Long id);

	Book updateBook(Book book);

	Book createBook(Book book);

	boolean deleteBook(Long id);

	Long getIdByName(String name);

	List<Book> getList(long offset, int size);

	Book getByName(String name);

	Map<Long, Book> getByIds(List<Long> nonCacheIds);

}
