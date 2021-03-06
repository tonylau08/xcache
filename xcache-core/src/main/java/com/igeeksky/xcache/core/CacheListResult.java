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

import java.util.List;
import java.util.Map;

/**
 * @author Tony.Lau
 * @blog: https://my.oschina.net/xcafe
 * @createTime 2017-03-02 20:12:08
 */
public class CacheListResult<E, T> {
	
	private List<E> allIds;
	
	private List<E> nonCacheIds;
	
	private Map<E, T> cacheElements;

	public List<E> getAllIds() {
		return allIds;
	}

	public void setAllIds(List<E> allIds) {
		this.allIds = allIds;
	}

	public List<E> getNonCacheIds() {
		return nonCacheIds;
	}

	public void setNonCacheIds(List<E> nonCacheIds) {
		this.nonCacheIds = nonCacheIds;
	}

	public Map<E, T> getCacheElements() {
		return cacheElements;
	}

	public void setCacheElements(Map<E, T> cacheElements) {
		this.cacheElements = cacheElements;
	}
	
}
