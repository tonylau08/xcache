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

package com.igeeksky.xcache.extend.redis;

import java.nio.charset.Charset;

import com.igeeksky.xcache.util.DigestUtils;

/**
 * @author Tony.Lau
 * @blog: https://my.oschina.net/xcafe
 * @createTime 2017-02-28 18:09:00
 */

public class RedisScript {
	
	private final String script;
	
	private final String sha;
	
	private final byte[] scriptBytes;
	
	private final byte[] shaBytes;
	
	private static final Charset UTF8_CHARSET = Charset.forName("UTF8");
	
	public RedisScript(String script){
		this.script = script;
		this.scriptBytes = script.getBytes(UTF8_CHARSET);
		this.sha = DigestUtils.SHA1(script);
		this.shaBytes = sha.getBytes(UTF8_CHARSET);
	}

	public String getScript() {
		return script;
	}

	public String getSha() {
		return sha;
	}

	public byte[] getScriptBytes() {
		return scriptBytes;
	}

	public byte[] getShaBytes() {
		return shaBytes;
	}

}
