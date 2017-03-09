/*
 * Copyright 2017 Tony.lau All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.igeeksky.xcache.support;

import org.springframework.util.Assert;

import com.KV;

/**
 * @author Tony.Lau
 * @blog: https://my.oschina.net/xcafe
 * @createTime 2017-03-05 08:02:29
 */

public class KeyValue {
	
	private Object id;
	
	private Object key;
	
	private Object value;
	
	private byte[] fullIdKeyBytes;
	
	private byte[] valueBytes;
	
	public KeyValue() { }

	public KeyValue(Object id, Object value) {
		Assert.notNull(id, "cache key must not be null");
		this.id = id;
		this.value = value;
	}
	
	public KV getKVWithId(){
		return new KV(id, key, value);
	}
	
	public KV getKV(){
		return new KV(key, value);
	}

	public Object getId() {
		return id;
	}

	public void setId(Object id) {
		this.id = id;
	}

	public Object getKey() {
		return key;
	}

	public void setKey(Object key) {
		this.key = key;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public byte[] getFullIdKeyBytes() {
		return fullIdKeyBytes;
	}

	public void setFullIdKeyBytes(byte[] fullIdKeyBytes) {
		this.fullIdKeyBytes = fullIdKeyBytes;
	}

	public byte[] getValueBytes() {
		return valueBytes;
	}

	public void setValueBytes(byte[] valueBytes) {
		this.valueBytes = valueBytes;
	}

}
