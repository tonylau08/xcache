package com.igeeksky.xcache.support;

import org.springframework.data.redis.connection.DataType;

public interface CacheKey {
	
	public String getCacheName();

	public DataType getDataType();

	public Module getModule();

	public long getAliveTime();

}
