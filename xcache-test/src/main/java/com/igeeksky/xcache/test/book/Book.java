package com.igeeksky.xcache.test.book;

import java.util.Arrays;

import com.igeeksky.xcache.support.BasePO;

public class Book implements BasePO {
	
	private static final long serialVersionUID = 2641028385063443941L;

	private int hash;
	
	private Long id;
	
	private String name;
	
	private Long version;
	
	private byte[] bytes;
	
	public Book() {
	}
	
	public Book(Long id, String name, byte[] bytes, Long version) {
		this.id = id;
		this.name = name;
		this.bytes = bytes;
		this.version = version;
	}

	public Book(Long id, String name, byte[] bytes) {
		this(id, name, bytes, null);
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
		return "{\"id\":\"" + id + "\", \"name\":\"" + name + "\", \"version\":\"" + version + "\", \"bytes\":\""
				+ Arrays.toString(bytes) + "\"}";
	}

	public byte[] getBytes() {
		return bytes;
	}

	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	@Override
	public int hashCode() {
		int h = hash;
		if(h == 0){
			h = (31 * id.hashCode() + name.hashCode()) * 31 + version.hashCode();
			return hash = h;
		}
		return h;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Book){
			Book b = (Book)obj;
			if(obj.hashCode() == hashCode()){
				if(b.id.equals(id) && b.name.equals(name)){
					return true;
				}
			}
		}
		return false;
	}
	

}
