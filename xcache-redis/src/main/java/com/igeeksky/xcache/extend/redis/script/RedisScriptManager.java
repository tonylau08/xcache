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

/**
 * @author Tony.Lau
 * @blog: https://my.oschina.net/xcafe
 * @createTime 2017-03-02 02:38:25
 */

public interface RedisScriptManager {

    String getName();

    Long getExpiration();

    RedisScript getPutCmpVerScript();

    RedisScript getPutIfAbsentScript();

    RedisScript getGetCmpVerScript();

    RedisScript getGetListCmpVerScript();

    RedisScript getEvitScript();

    RedisScript getClearScript();

}