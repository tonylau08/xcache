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

package com.igeeksky.xcache.exception;

import org.springframework.core.NestedRuntimeException;

/**
 * @author Tony.Lau
 * @blog: https://my.oschina.net/xcafe
 * @createTime 2017-03-07 07:58:20
 */
public class NullOrEmptyKeyException extends NestedRuntimeException {

	private static final long serialVersionUID = -1448602051163305877L;
	
	public NullOrEmptyKeyException() {
        super("Key must not be null or empty or blank string");
    }
	
	public NullOrEmptyKeyException(String message) {
        super(message);
    }
	
	public NullOrEmptyKeyException(String message, Throwable cause) {
        super(message, cause);
    }

}
