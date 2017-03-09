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

package com.igeeksky.xcache.support.gen;

import com.igeeksky.xcache.exception.NullOrEmptyKeyException;
import com.igeeksky.xcache.util.StringUtils;

/**
 * @author Tony.Lau
 * @blog: https://my.oschina.net/xcafe
 * @createTime 2017-03-02 19:58:00
 */

public class KeyGen {
	
	public static String keyGen(Object...params){
		StringBuilder builder = new StringBuilder();
		for(int i=0; i<params.length; i++){
			Object temp = params[i];
			if(null != temp){
				builder.append(temp);
			}
		}
		String key = builder.toString();
		if(StringUtils.isNotEmpty(key)){
			return key;
		}
		throw new NullOrEmptyKeyException();
	}
	
	private KeyGen(){}

}
