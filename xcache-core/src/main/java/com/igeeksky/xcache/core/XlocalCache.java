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

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.util.Assert;

import com.igeeksky.dcafe.snowflake.TimeGen;
import com.igeeksky.xcache.support.CacheKey;

/**
 * 
 * @author Tony.Lau
 * @blog: https://my.oschina.net/xcafe
 * @createTime 2017-02-21 18:38:43
 */
public class XlocalCache implements Cache {
	
	private Logger logger = LoggerFactory.getLogger(XlocalCache.class);

	private final ConcurrentMap<Object, LocalValueWrapper> store;
	
	private final ConcurrentMap<Object, VisitRecord> timeStore;
	
	private final String name;
	
	private final long aliveTime;
	
	private final int maxElementsNum;
	
	/** 元素数量警告(最大元素数量的80%) */
	private final int warnElementsNum;
	
	/** 容器初始容量(最大元素数量的50%) */
	private final int initialCapacity;
	
	private final AtomicInteger currElementsNum;
	
	private final boolean allowNullValues = false;
	
	private final ScheduledExecutorService executor;
	
	private final XLocalCacheCleanner cleanner;
	
	private volatile boolean isCleaning = false;
	
	private final Lock lock = new ReentrantLock();
	
	XlocalCache(CacheKey cacheKey, int maxElementsNum, ScheduledExecutorService executor){
		this(cacheKey.getCacheName(), cacheKey.getAliveTime(), maxElementsNum, executor);
	}
	
	XlocalCache(String name, long aliveTime, int maxElementsNum, ScheduledExecutorService executor) {
		this.name = name;
		this.aliveTime = aliveTime * 1000;
		this.maxElementsNum = maxElementsNum > 256 ? maxElementsNum : 256;
		this.warnElementsNum = maxElementsNum / 5 * 4;
		this.initialCapacity =  maxElementsNum / 2;
		this.currElementsNum = new AtomicInteger(0);
		
		this.store = new ConcurrentHashMap<Object, LocalValueWrapper>(initialCapacity);
		this.timeStore = new ConcurrentHashMap<Object, VisitRecord>(initialCapacity);
		
		this.executor = executor;
		this.cleanner = new XLocalCacheCleanner(this);
	}
	
	public int getMaxElementsNum() {
		return maxElementsNum;
	}
	
	public int getCurrElementsNum() {
		return currElementsNum.get();
	}

	public int getWarnElementsNum() {
		return warnElementsNum;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Object getNativeCache() {
		return store;
	}

	@Override
	public ValueWrapper get(Object key) {
		ValueWrapper value = store.get(key);
		
		/* 记录最后访问时间和增加访问次数 */
		if(value != null){
			VisitRecord record = timeStore.get(key);
			timeStore.put(key, (record == null) ? new VisitRecord() : record.update());
		}
		
		return value;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(Object key, Class<T> type) {
		ValueWrapper value = get(key);
		if(value == null) return null;
		
		if (type != null && !type.isInstance(value.get())) {
			throw new IllegalStateException("Cached value is not of required type [" + type.getName() + "]: " + value);
		}
		return (T) (value.get());
	}

	@Override
	public void put(Object key, Object value) {
		Assert.notNull(value, "Cache value must not be null");
		LocalValueWrapper wrapper = this.store.put(key, toValueWrapper(value));
		if(null == wrapper){
			computeAndClean();
		}
	}

	@Override
	public ValueWrapper putIfAbsent(Object key, Object value) {
		Assert.notNull(value, "Cache value must not be null");
		Object existing = this.store.putIfAbsent(key, toValueWrapper(value));
		if(null == existing){
			computeAndClean();
		}
		return toValueWrapper(existing);
	}

	private void computeAndClean() {
		if(currElementsNum.incrementAndGet() > warnElementsNum && !isCleaning){
			isCleaning = true;
			executor.submit(cleanner);
			/*try{
				lock.lock();
				if(!isCleaning){
					
				}
			} finally{
				lock.unlock();
			}*/
		}
	}
	
	private LocalValueWrapper toValueWrapper(Object value) {
		return new LocalValueWrapper(TimeGen.INSTANCE.currTime()+aliveTime, value);
	}

	@Override
	public void evict(Object key) {
		LocalValueWrapper wrapper = this.store.remove(key);
		this.timeStore.remove(key);
		if(null != wrapper){
			currElementsNum.decrementAndGet();
		}
	}

	@Override
	public void clear() {
		this.store.clear();
		this.timeStore.clear();
		currElementsNum.set(0);
	}
	
	public final boolean isAllowNullValues() {
		return this.allowNullValues;
	}
	
	/** 清理过期数据和不活跃数据 */
	void clean(){
		logger.info(name + "_localCacheClean start");
		try{
			isCleaning = true;
		
			int size = store.size();
			logger.info(name + "_size:" + size);
			if(size < warnElementsNum){
				currElementsNum.set(size);
				isCleaning = false;
				return;
			}
			
			if(this.cleanExpireElements()){
				if(this.lru()){
					if(this.randomClean()){
						this.clear();
					}
				}
			}
			
			currElementsNum.set(store.size());
		}finally{
			isCleaning = false;
			logger.info(name + "_localCacheClean end");
		}
	}
	
	/** 获取平均访问次数的一半 */
	private int getHalfAvgTimes(){
		Iterator<Entry<Object, VisitRecord>> it = timeStore.entrySet().iterator();
		int index = 1;
		int totalTimes = 0;
		while(it.hasNext() && index < 10000){
			Entry<Object, VisitRecord> entry = it.next();
			totalTimes += entry.getValue().visitTimes;
			index++;
		}
		int avg = totalTimes / index / 2;
		return avg > 2 ? avg : 2;
	}
	
	/**
	 * 清理过期元素
	 * @return true:大于警告容量；false：小于等于警告容量
	 */
	private boolean cleanExpireElements(){
		Iterator<Entry<Object, LocalValueWrapper>> it = store.entrySet().iterator();
		long currentTime = System.currentTimeMillis();
		long perridTime = aliveTime >> 2;
		
		while(it.hasNext()){
			Entry<Object, LocalValueWrapper> entry = it.next();
			LocalValueWrapper wrapper;
			if(null != entry && null != (wrapper = entry.getValue())){
				//如果已过期，删除
				long expireTime = wrapper.getExpireTime();
				if(wrapper.getExpireTime() < currentTime){
					it.remove();
					timeStore.remove(entry.getKey());
					currElementsNum.decrementAndGet();
				}
				
				//如果超过存活时间的1/4无访问记录，删除
				Object key = entry.getKey();
				if(null != key){
					VisitRecord record = timeStore.get(key);
					if(null == record){
						if((expireTime - aliveTime + perridTime) < currentTime){
							it.remove();
							timeStore.remove(key);
							currElementsNum.decrementAndGet();
						}
					}
				}
				
			}
			
		}
		logger.info(name + "_cleanExpireElements() end StoreSize:" + store.size());
		return (store.size() > warnElementsNum);
	}
	
	private boolean lru(){
		Iterator<Entry<Object, VisitRecord>> it = timeStore.entrySet().iterator();
		long currentTime = System.currentTimeMillis();
		long perridTime = aliveTime  >> 2;
		int halfAvgTimes = getHalfAvgTimes();
		
		while(it.hasNext()){
			Entry<Object, VisitRecord> entry = it.next();
			VisitRecord record ;
			if(null != entry && null != (record = entry.getValue()) ){
				//如果 (当前时间 - 最近访问时间) > 最大存活时间的1/4
				if((currentTime - record.lastTime) > perridTime){
					//如果访问次数 < 平均访问次数的一半
					if(record.visitTimes < halfAvgTimes){
						it.remove();
						store.remove(entry.getKey());
						currElementsNum.decrementAndGet();
					}
				}
			}
		}
		logger.info(name + "_lru() end StoreSize:" + store.size());
		return (store.size() > warnElementsNum);
	}
	
	private boolean randomClean(){
		int size = store.size();
		Iterator<Entry<Object, LocalValueWrapper>> it = store.entrySet().iterator();
		while(it.hasNext() && size > initialCapacity){
			Entry<Object, LocalValueWrapper> entry = it.next();
			if(null != entry){
				it.remove();
				timeStore.remove(entry.getKey());
				currElementsNum.decrementAndGet();
				size--;
			}
		}
		logger.info(name + "_randomClean() end StoreSize:" + store.size());
		return (store.size() > warnElementsNum);
	}
	
	private static class VisitRecord{
		
		public long lastTime = TimeGen.INSTANCE.currTime();
		
		public int visitTimes = 1;

		public VisitRecord update(){
			this.lastTime = TimeGen.INSTANCE.currTime();
			visitTimes++;
			return this;
		}
	}
	
	static class LocalValueWrapper implements ValueWrapper{
		
		/** 到期时间 */
		private long expireTime;
		
		private Object obj;
		
		public LocalValueWrapper(long expireTime, Object obj) {
			this.expireTime = expireTime;
			this.obj = obj;
		}

		public long getExpireTime() {
			return expireTime;
		}

		public void setExpireTime(long expireTime) {
			this.expireTime = expireTime;
		}

		public Object getObj() {
			return obj;
		}

		public void setObj(Object obj) {
			this.obj = obj;
		}

		@Override
		public Object get() {
			return obj;
		}
	}

}
