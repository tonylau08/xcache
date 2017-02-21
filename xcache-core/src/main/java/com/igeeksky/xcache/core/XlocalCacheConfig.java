package com.igeeksky.xcache.core;

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
		return "ClairLocalCacheArgs [name=" + name + ", aliveTime=" + aliveTime + ", singleStoreMaxElements="
				+ singleStoreMaxElements + "]";
	}
	
}
