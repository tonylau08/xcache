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

/**
<b>获取本地版本，比较远程版本</b><br> 
r['s']=0：本地值存在，远程值不存在 					（删除本地缓存，返回null）<br>
r['s']=1：本地值存在，远程值存在，远程版本等于本地版本（返回本地缓存)<br>
r['s']=2：本地值存在，远程值存在，远程版本大于本地版本（获取远程缓存，保存到本地缓存并返回）<br>
r['s']=3：本地值存在，远程值存在，远程版本小于本地版本（删除远程缓存，删除远程HKEYS集合元素，删除本地缓存，返回null）<br> 
r['s']=4：本地值不存在，远程值存在					（获取远程缓存，保存到本地缓存并返回）<br>
r['s']=5：本地值不存在，远程值不存在 					（返回null）<br>


KEYS[1]: I_KEY,	//C_SYSUSER~I:ID	执行
ARGV[1]: KV[]
(
KV[i]['k']	key
KV[i]['v']  version
)


集群版本 getListCmpVer
local r_list = {};r_list['@class']='java.util.HashMap';local kvs = cjson.decode(ARGV[1]);
for i in ipairs(kvs) do 
	local r={};r['@class']='com.igeeksky.xcache.core.R';local kv = kvs[i];local k = kv['k'];local local_v = kv['v'];
	if 1==redis.call('EXISTS',k) then 
		local obj = redis.call('GET',k);
		if type(local_v) == 'number' then 
			local val = cjson.decode(obj);
			local remote_v = val['version'];
			if remote_v>local_v then 
				r['s']=2;
				r['o']=obj;
			elseif remote_v==local_v then 
				r['s']=1;
			else 
				redis.call('DEL',k);
				r['s']=3;
			end;
		else 
			r['s']=4;
			r['o']=obj;
		end;
	else 
		if type(local_v) == 'number' then 
			r['s']=0;
		else 
			r['s']=5;
		end;
	end;
	r_list[k] = r;
end;
return cjson.encode(r_list);
*/

package com.igeeksky.xcache.extend.redis.script;

import com.igeeksky.xcache.extend.redis.script.RedisScript;
import com.igeeksky.xcache.extend.redis.script.RedisScriptManager;

/**
 * @author Tony.Lau
 * @blog: https://my.oschina.net/xcafe
 * @createTime 2017-03-02 02:22:50
 */
public class RedisClusterRedisScriptManager implements RedisScriptManager {
	
	private final String name;
	
	private final Long expiration;
	
	private String putCmpVer = "local I_KEY=KEYS[1];local VALUE=ARGV[1];local VERSION=ARGV[2]; if 1==redis.call('EXISTS',I_KEY) then local new_ver = tonumber(VERSION); if type(new_ver) == 'nil' then redis.call('DEL',I_KEY); return 4; end; local old_val = cjson.decode(redis.call('GET',I_KEY)); local old_ver = tonumber(old_val['version']); if type(old_ver) == 'nil' then redis.call('DEL',I_KEY); return 5; end; if old_ver>new_ver then return 0; elseif old_ver==new_ver then return 3; else redis.call('SETEX',I_KEY, 86400, VALUE); return 2; end; else redis.call('SETEX',I_KEY, 86400, VALUE); return 1; end;";

	private String getCmpVer = "local r={};r['@class']='com.igeeksky.xcache.core.R';local I_KEY=KEYS[1];local VERSION=ARGV[1];if 1==redis.call('EXISTS',I_KEY) then local local_v = tonumber(VERSION);local obj = redis.call('GET',I_KEY);local val = cjson.decode(obj);local remote_v = val['version'];if remote_v>local_v then r['s']=2;r['v']=remote_v;r['o']=obj;elseif remote_v==local_v then r['s']=1;else redis.call('DEL',I_KEY);r['s']=3;end;else r['s']=0;end;return cjson.encode(r);";
	
	private String getListCmpVer = "local r_list = {};r_list['@class']='java.util.HashMap';local kvs = cjson.decode(ARGV[1]); for i in ipairs(kvs) do local r={};r['@class']='com.igeeksky.xcache.core.R';local kv = kvs[i];local k = kv['k'];local local_v = kv['v']; if 1==redis.call('EXISTS',k) then local obj = redis.call('GET',k); if type(local_v) == 'number' then local val = cjson.decode(obj); local remote_v = val['version']; if remote_v>local_v then r['s']=2; r['o']=obj; elseif remote_v==local_v then r['s']=1; else redis.call('DEL',k); r['s']=3; end; else r['s']=4; r['o']=obj; end; else if type(local_v) == 'number' then r['s']=0; else r['s']=5; end; end; r_list[k] = r; end; return cjson.encode(r_list);";

	private String putIfAbsent = "if 1==redis.call('EXISTS',KEYS[1]) then return 0;else redis.call('SETEX',I_KEY, 86400, ARGV[1]);return 1;end;";
	
	/*private String evit = null;
	
	private String clear = null;*/
	
	private RedisScript putCmpVerScript;
	private RedisScript putIfAbsentScript;
	private RedisScript getCmpVerScript;
	private RedisScript getListCmpVerScript;
	/*private RedisScript evitScript;
	private RedisScript clearScript;*/
	
	public RedisClusterRedisScriptManager(String name, long expiration){
		this.name = name;
		this.expiration = expiration;
		this.putCmpVer = putCmpVer.replace("##expiration##", String.valueOf(expiration));
		this.getCmpVer = getCmpVer.replace("##expiration##", String.valueOf(expiration));
		this.getListCmpVer = getListCmpVer.replace("##expiration##", String.valueOf(expiration));
		/*this.evit = evit.replace("##expiration##", String.valueOf(expiration));
		this.clear = clear.replace("##expiration##", String.valueOf(expiration));*/
		this.putCmpVerScript = new RedisScript(putCmpVer);
		this.putIfAbsentScript = new RedisScript(putIfAbsent);
		this.getCmpVerScript = new RedisScript(getCmpVer);
		this.getListCmpVerScript = new RedisScript(getListCmpVer);
		/*this.evitScript = null;
		this.clearScript = null;*/
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
	
	public RedisScript getGetListCmpVerScript() {
		return getListCmpVerScript;
	}

	public RedisScript getEvitScript() {
		throw new UnsupportedOperationException("RedisCluster is not support evit script, please use del(key[]) command");
	}

	public RedisScript getClearScript() {
		throw new UnsupportedOperationException("RedisCluster is not support clear script, please use del(key[]) command");
	}

}
