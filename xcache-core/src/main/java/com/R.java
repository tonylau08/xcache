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

package com;

import org.springframework.cache.Cache.ValueWrapper;

/**
 * @author Tony.Lau
 * @blog: https://my.oschina.net/xcafe
 * @createTime 2017-02-28 01:06:12
 */

public class R implements ValueWrapper {

	/** 状态码 */
	public Integer s;

	/** 版本 */
	public Long v;

	/** 缓存对象 */
	public Object o;
	
	/** Key */
	public Object k;

	public Integer getS() {
		return s;
	}

	public void setS(Integer s) {
		this.s = s;
	}

	public Long getV() {
		return v;
	}

	public void setV(Long v) {
		this.v = v;
	}

	public Object getO() {
		return o;
	}

	public void setO(Object o) {
		this.o = o;
	}

	@Override
	public Object get() {
		return o;
	}
	
	public Object getK() {
		return k;
	}

	public void setK(Object k) {
		this.k = k;
	}

	@Override
	public String toString() {
		return "{\"s\":\"" + s + "\", \"v\":\"" + v + "\", \"o\":\"" + o + "\", \"k\":\"" + k + "\"}";
	}

}
