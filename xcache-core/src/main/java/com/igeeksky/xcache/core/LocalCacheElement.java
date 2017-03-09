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

import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.util.Assert;

/**
 * @author Tony.Lau
 * @blog: https://my.oschina.net/xcafe
 * @createTime 2017-02-27 17:43:55
 */

public class LocalCacheElement implements ValueWrapper {

	private Long version;
	
	private Object obj;
	
	public LocalCacheElement(Long version, Object obj) {
		Assert.notNull(version, "version must not be null");
		Assert.notNull(obj, "value must not be null");
		this.version = version;
		this.obj = obj;
	}

	public Long getVersion() {
		return version;
	}

	public LocalCacheElement setVersion(Long version) {
		this.version = version;
		return this;
	}

	public Object getObj() {
		return obj;
	}

	public LocalCacheElement setObj(Object obj) {
		this.obj = obj;
		return this;
	}

	@Override
	public Object get() {
		return obj;
	}
	
}
