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

package com.igeeksky.xcache.extend.redis.script;

import com.igeeksky.xcache.extend.redis.script.RedisScript;
import com.igeeksky.xcache.extend.redis.script.RedisScriptManager;

/**
 * @author Tony.Lau
 * @blog: https://my.oschina.net/xcafe
 * @createTime 2017-03-02 02:22:50
 */
public class RedisSingleRedisScriptManager implements RedisScriptManager {
	
	private final String name;
	
	private final Long expiration;
	
	private String putCmpVer = " local I_KEY=KEYS[1];local KEY_SET=KEYS[2];local VALUE=ARGV[1];local VERSION=ARGV[2];local ID=ARGV[3]; if 1==redis.call('EXISTS',I_KEY) then local new_ver = tonumber(VERSION); if type(new_ver) == 'nil' then redis.call('DEL',I_KEY); redis.call('HDEL',KEY_SET, ID); return 4; end; local old_val = cjson.decode(redis.call('GET',I_KEY)); local old_ver = tonumber(old_val['version']); if type(old_ver) == 'nil' then redis.call('DEL',I_KEY); redis.call('HDEL',KEY_SET, ID); return 5; end; if old_ver>new_ver then return 0; elseif old_ver==new_ver then return 3; else redis.call('SETEX',I_KEY, 86400, VALUE); return 2; end; else redis.call('SETEX',I_KEY, 86400, VALUE); redis.call('HSET',KEY_SET, ID, 1); return 1; end;";
	
	private String putIfAbsent = "local I_KEY=KEYS[1];local KEY_SET=KEYS[2];local VALUE=ARGV[1];local ID=ARGV[2]; if 1==redis.call('EXISTS',KEYS[1]) then return 0; else redis.call('SETEX',I_KEY, 86400, ARGV[1]); redis.call('HSET',KEY_SET, ID, 1); return 1; end;";
	
	private String getCmpVer = "local r={};r['@class']='com.igeeksky.xcache.core.R';local I_KEY=KEYS[1];local KEY_SET=KEYS[2];local VERSION=ARGV[1];local ID=ARGV[2]; if 1==redis.call('EXISTS',I_KEY) then local local_v = tonumber(VERSION); local obj = redis.call('GET',I_KEY); local val = cjson.decode(obj); local remote_v = val['version']; if remote_v>local_v then r['s']=2; r['v']=remote_v; r['o']=obj; elseif remote_v==local_v then r['s']=1; else redis.call('DEL',I_KEY); redis.call('HDEL',KEY_SET, ID); r['s']=3; end; else r['s']=0; end; return cjson.encode(r);";
	
	private String getListCmpVer = "local r_list = {};r_list['@class']='java.util.HashMap';local kvs = cjson.decode(ARGV[1]);local KEY_SET=KEYS[1]; for i in ipairs(kvs) do local r={};r['@class']='com.igeeksky.xcache.core.R';local kv = kvs[i];local k = kv['k'];local id=kv['id'];local local_v = kv['v']; if 1==redis.call('EXISTS',k) then local obj = redis.call('GET',k); if type(local_v) == 'number' then local val = cjson.decode(obj); local remote_v = val['version']; if remote_v>local_v then r['s']=2; r['o']=obj; elseif remote_v==local_v then r['s']=1; else redis.call('DEL',k); redis.call('HDEL', KEY_SET, id); r['s']=3; end; else r['s']=4; r['o']=obj; end; else if type(local_v) == 'number' then r['s']=0; else r['s']=5; end; end; r_list[k] = r; end; return cjson.encode(r_list);";
	
	private String evit = "redis.call('DEL',KEYS[1]);redis.call('HDEL',KEYS[2], ARGV[1]);";
	
	private String clear = "local KEY_SET = KEYS[1]; local LIST_KEY=KEYS[2]; local REL_KEY=KEYS[3]; local PREFIX=ARGV[1]; local del_num=0; if 1==redis.call('EXISTS',KEY_SET) then local all_key = redis.call('HKEYS',KEY_SET); if type(all_key)=='table' then for i in ipairs(all_key) do local full_Key = PREFIX..all_key[i]; del_num = del_num + redis.call('DEL', full_Key); end; end; end; redis.call('DEL', KEY_SET);redis.call('DEL', LIST_KEY);redis.call('DEL', REL_KEY); return del_num;";
	
	private RedisScript putCmpVerScript;
	private RedisScript putIfAbsentScript;
	private RedisScript getCmpVerScript;
	private RedisScript getListCmpVerScript;
	private RedisScript evitScript;
	private RedisScript clearScript;

	public RedisSingleRedisScriptManager(String name, long expiration){
		this.name = name;
		this.expiration = expiration;
		this.putCmpVer = putCmpVer.replace("##expiration##", String.valueOf(expiration));
		this.putIfAbsent = putIfAbsent.replace("##expiration##", String.valueOf(expiration));
		this.getCmpVer = getCmpVer.replace("##expiration##", String.valueOf(expiration));
		this.getListCmpVer = getListCmpVer.replace("##expiration##", String.valueOf(expiration));
		this.evit = evit.replace("##expiration##", String.valueOf(expiration));
		this.clear = clear.replace("##expiration##", String.valueOf(expiration));
		this.putCmpVerScript = new RedisScript(putCmpVer);
		this.putIfAbsentScript = new RedisScript(putIfAbsent);
		this.getCmpVerScript = new RedisScript(getCmpVer);
		this.getListCmpVerScript = new RedisScript(getListCmpVer);
		this.evitScript = new RedisScript(evit);
		this.clearScript = new RedisScript(clear);
	}

	public String getName() {
		return name;
	}

	public Long getExpiration() {
		return expiration;
	}

	public RedisScript getPutCmpVerScript() {
		return putCmpVerScript;
	}

	public RedisScript getPutIfAbsentScript() {
		return putIfAbsentScript;
	}

	public RedisScript getGetCmpVerScript() {
		return getCmpVerScript;
	}

	public RedisScript getGetListCmpVerScript(){
		return getListCmpVerScript;
	}

	public RedisScript getEvitScript() {
		return evitScript;
	}

	public RedisScript getClearScript() {
		return clearScript;
	}

}
