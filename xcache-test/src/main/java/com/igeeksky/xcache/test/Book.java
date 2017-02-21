package com.igeeksky.xcache.test;

import java.io.Serializable;

public class Book implements Serializable {
	
	private static final long serialVersionUID = 8639766712748613248L;

	private Long id;
	
	private String name;
	
	public Book() {
		super();
	}

	public Book(Long id, String name) {
		this.id = id;
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "book:{id:" + id + ", name:" + name + "}";
	}
	
}
