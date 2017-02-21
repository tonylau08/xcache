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

package com.igeeksky.xcache.core;

/**
 * 
 * @author Tony.Lau
 * @blog: https://my.oschina.net/xcafe
 * @createTime 2017-02-21 18:39:06
 */
public class XlocalCacheConfig {
	
	private String name;
	
	private Long aliveTime;
	
	private Integer singleStoreMaxElements = 1024;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getAliveTime() {
		return aliveTime;
	}

	public void setAliveTime(Long aliveTime) {
		this.aliveTime = aliveTime;
	}

	public Integer getSingleStoreMaxElements() {
		return singleStoreMaxElements;
	}

	public void setSingleStoreMaxElements(Integer singleStoreMaxElements) {
		this.singleStoreMaxElements = singleStoreMaxElements;
	}

	@Override
	public String toString() {
		return "XlocalCacheConfig [name=" + name + ", aliveTime=" + aliveTime + ", singleStoreMaxElements="
				+ singleStoreMaxElements + "]";
	}
	
}
