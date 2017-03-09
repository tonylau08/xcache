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

import org.springframework.cache.Cache;

import com.R;
import com.igeeksky.xcache.support.KeyValue;

/**
 * @author Tony.Lau
 * @blog: https://my.oschina.net/xcafe
 * @createTime 2017-02-23 01:12:15
 */
public interface RemoteCache extends Cache {

	/**
	 * <b>保存缓存，比较新缓存和旧缓存的版本号</b><br>
	 * 1.远程值不存在							（保存新版本，保存本地缓存，返回1) <br>
	 * 0.远程值存在，旧版本号大于新版本号		（保留旧版本，删除本地缓存，返回0) <br>
	 * 2.远程值存在，旧版本号小于新版本号		（保存新版本，保存本地缓存，返回2）<br>
	 * 3.远程值存在，旧版本号等于新版本号		（保留旧版本，删除本地缓存，返回3）<br>
	 * 4.新版本号为空							（删除旧版本，删除本地缓存，返回4，抛出异常）<br>
	 * 5.旧版本号为空							（删除旧版本，删除本地缓存，返回5，抛出异常）<br>
	 * 3.1 返回3一可能是程序错误，未更新数据库版本；<br>
	 * 3.2 返回3二可能是多处使用@CachePut注解，为避免未更新到数据库的数值存入缓存，选择保留原值。<br>
	 */
	int putWithVersion(Object key, Object value, Long version);

	/**
	 * <b>获取本地版本，比较远程版本</b><br>
	 * r['s']=0：远程值不存在						（删除本地缓存，返回null）<br>
	 * r['s']=1：远程值存在，远程版本等于本地版本	（返回本地缓存)<br>
	 * r['s']=2：远程值存在，远程版本大于本地版本	（获取远程缓存，保存到本地缓存并返回）<br>
	 * r['s']=3：远程值存在，远程版本小于本地版本	（删除远程缓存，删除远程HKEYS集合元素，删除本地缓存，返回null）<br>
	 */
	R getWithVersion(Object key, Long localVersion);

	ValueWrapper getRelation(Object key);

	void putRelation(Object key, Object id);

	ValueWrapper getRelationId(Object key);

	<T>void putList(Object key, List<T> ids);

	ValueWrapper getIds(Object key);

	/**
	 * <b>获取本地版本，比较远程版本</b><br> 
	 * r['s']=0：本地值存在，远程值不存在 						（删除本地缓存，返回null）<br>
	 * r['s']=1：本地值存在，远程值存在，远程版本等于本地版本 	（返回本地缓存)<br>
	 * r['s']=2：本地值存在，远程值存在，远程版本大于本地版本 	（获取远程缓存，保存到本地缓存并返回）<br>
	 * r['s']=3：本地值存在，远程值存在，远程版本小于本地版本 	（删除远程缓存，删除远程HKEYS集合元素，删除本地缓存，返回null）<br> 
	 * r['s']=4：本地值不存在，远程值存在						（获取远程缓存，保存到本地缓存并返回）<br>
	 * r['s']=5：本地值不存在，远程值不存在 					（返回null）<br>
	 */
	Map<Object, R> getListCmpVersion(KeyValue[] kvs);

	String putOther(Object key, int second, Object value);

	ValueWrapper getOther(Object key);

	void delOther(Object key);

}
