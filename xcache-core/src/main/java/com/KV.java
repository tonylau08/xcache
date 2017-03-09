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

/**
 * @author Tony.Lau
 * @blog: https://my.oschina.net/xcafe
 * @createTime 2017-03-02 21:37:52
 */
public class KV {
	
	private Object id;
	
	private Object k;
	
	private Object v;

	public KV() {}

	public KV(Object k, Object v) {
		this.k = k;
		this.v = v;
	}

	public KV(Object id, Object k, Object v) {
		this.id = id;
		this.k = k;
		this.v = v;
	}

	public Object getId() {
		return id;
	}

	public void setId(Object id) {
		this.id = id;
	}
	
	public Object getK() {
		return k;
	}

	public void setK(Object k) {
		this.k = k;
	}

	public Object getV() {
		return v;
	}

	public void setV(Object v) {
		this.v = v;
	}
	
}
