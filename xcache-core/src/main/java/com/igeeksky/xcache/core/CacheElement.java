package com.igeeksky.xcache.core;

import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.util.Assert;

public class CacheElement {

	private Long version;

	private ValueWrapper valueWrapper;

	public CacheElement(Long version, ValueWrapper valueWrapper) {
		Assert.notNull(version, "version must not be null");
		Assert.notNull(valueWrapper, "valueWrapper must not be null");
		this.version = version;
		this.valueWrapper = valueWrapper;
	}

	public long getVersion() {
		return version;
	}

	public CacheElement setVersion(Long version) {
		Assert.notNull(version, "version must not be null");
		this.version = version;
		return this;
	}

	public ValueWrapper getValueWrapper() {
		return valueWrapper;
	}

	public void setValueWrapper(ValueWrapper valueWrapper) {
		Assert.notNull(valueWrapper, "valueWrapper must not be null");
		this.valueWrapper = valueWrapper;
	}

}