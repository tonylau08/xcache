package com.igeeksky.xcache.extend.redis;

import java.util.Arrays;

import com.igeeksky.xcache.support.serializer.KeySerializer;
import com.igeeksky.xcache.util.BytesUtils;

public class RedisCacheMetadata {

	/** 缓存名称 */
	private final String name;
	
	/**
	 * 以ID为key的缓存前缀
	 * Redis类型：string
	 */
	private final String idKey;
	
	/**
	 * 所有以ID为Key的缓存的KeySet
	 * Redis类型：hash
	 */
	private final String keySet;
	
	/**
	 * 分页/列表的ID缓存 (如getList_20_10  ：id[] )
	 * Redis类型：hash
	 */
	private final String listKey;
	
	/**
	 * 关联关系的ID缓存（如 username : id）
	 * Redis类型：hash
	 */
	private final String relateKey;
	
	/**
	 * 其它key的缓存前缀
	 * Redis类型：string
	 */
	private final String otherKey;
	
	/**  */
	private final byte[] nameBytes;
	
	/**  */
	private final byte[] idKeyBytes;
	
	/**  */
	private final byte[] keySetBytes;
	
	/**  */
	private final byte[] listKeyBytes;
	
	/**  */
	private final byte[] relateKeyBytes;
	
	/**  */
	private final byte[] otherKeyBytes;
	
	private KeySerializer<Object, Object> keySerializer;

	public RedisCacheMetadata(String name, KeySerializer<Object, Object> keySerializer) {
		this.name = name;
		this.idKey = name + "~I:";
		this.keySet = name + "~KEYSET";
		this.listKey = name + "~LIST";
		this.relateKey = name + "~RELATION";
		this.otherKey = name + "~O:";
		
		this.nameBytes = name.getBytes();
		this.idKeyBytes = idKey.getBytes();
		this.keySetBytes = keySet.getBytes();
		this.listKeyBytes = listKey.getBytes();
		this.relateKeyBytes = relateKey.getBytes();
		this.otherKeyBytes = otherKey.getBytes();
		
		this.keySerializer = keySerializer;
	}

	/** 缓存名称 */
	public String getName() {
		return name;
	}

	/** 以ID为key的缓存前缀 */
	public String getIdKey() {
		return idKey;
	}

	/** ID_KEY的集合 */
	public String getKeySet() {
		return keySet;
	}

	/** 缓存List数据的KEY */
	public String getListKey() {
		return listKey;
	}

	/** id与属性对应的关联KEY */
	public String getRelateKey() {
		return relateKey;
	}

	/** 其它缓存的KEY */
	public String getOtherKey() {
		return otherKey;
	}

	/** 缓存名称的字节数组 */
	public byte[] getNameBytes() {
		return nameBytes;
	}
	
	/** 以ID为key的缓存前缀 */
	public byte[] getIdKeyPrifixBytes() {
		return idKeyBytes;
	}
	
	/** 获取去除前缀之后的ID bytes */
	public byte[] getSuffixIdBytes(byte[] fullIdKeyBytes){
		if(fullIdKeyBytes.length < idKeyBytes.length){
			throw new IllegalArgumentException("keyBytes less than pfefix bytes: " + new String(fullIdKeyBytes));
		}
		return Arrays.copyOfRange(fullIdKeyBytes, idKeyBytes.length, fullIdKeyBytes.length);
	}
	
	/** 以ID为key的缓存Key全称 */
	public byte[] getFullIdKeyBytes(Object key) {
		return BytesUtils.merge(idKeyBytes, keySerializer.serialize(key));
	}

	/** ID_KEY的集合 */
	public byte[] getKeySetBytes() {
		return keySetBytes;
	}

	/** 缓存List数据的KEY */
	public byte[] getListKeyBytes() {
		return listKeyBytes;
	}

	/** id与属性对应的关联KEY */
	public byte[] getRelateKeyBytes() {
		return relateKeyBytes;
	}

	/** 其它缓存的KEY前缀 */
	public byte[] getOtherKeyBytes() {
		return otherKeyBytes;
	}
	
	/** 其它缓存的KEY的全部bytes */
	public byte[] getFullOtherKeyBytes(Object key) {
		return BytesUtils.merge(otherKeyBytes, keySerializer.serialize(key));
	}

}