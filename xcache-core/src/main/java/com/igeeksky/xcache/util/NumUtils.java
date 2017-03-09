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

package com.igeeksky.xcache.util;

/**
 * @author Tony.Lau
 * @blog: https://my.oschina.net/xcafe
 * @createTime 2017-02-28 15:59:37
 */
public class NumUtils {
	
	public static Long getLong(Object value){
		return getLong(value, value.getClass());
	}
	
	public static Long getLong(Object value, Class<? extends Object> clazz){
		if(null == value){
			return null;
		}
		if(clazz == Long.class || clazz == long.class){
			return(Long) value;
		}else if(clazz == Integer.class || clazz == int.class){
			return Long.valueOf((Integer)value);
		}else if(clazz == Byte.class || clazz == byte.class){
			return Long.valueOf((Byte)value);
		}else if(clazz == Short.class || clazz == short.class){
			return Long.valueOf((Short)value);
		}else{
			return Long.parseLong(value.toString());
		}
	}
	
	public static Integer getInteger(Object value){
		return getInteger(value, value.getClass());
	}
	
	public static Integer getInteger(Object value, Class<? extends Object> clazz){
		if(clazz == Long.class || clazz == long.class){
			return ((Long) value).intValue();
		}else if(clazz == Integer.class || clazz == int.class){
			return Integer.valueOf((Integer)value);
		}else if(clazz == Byte.class || clazz == byte.class){
			return Integer.valueOf((Byte)value);
		}else if(clazz == Short.class || clazz == short.class){
			return Integer.valueOf((Short)value);
		}else{
			return Integer.parseInt(value.toString());
		}
	}
	
	private NumUtils(){}

}
