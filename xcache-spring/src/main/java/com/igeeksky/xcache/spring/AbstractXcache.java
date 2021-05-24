/*
 * Copyright 2017-2020 the original author or authors.
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

package com.igeeksky.xcache.spring;

import com.igeeksky.xcache.core.support.LockSupport;
import org.springframework.util.StringUtils;


/**
 * @author Patrick.Lau
 * @date 2020-12-10
 */
public abstract class AbstractXcache implements Xcache {

    private String name;

    protected LockSupport lockSupport;

    protected AbstractXcache(String name, int backSourceSize) {
        if (!StringUtils.hasText(name)) {
            throw new IllegalStateException("cache name must not be null");
        }
        this.name = name.trim();
        this.lockSupport = new LockSupport(backSourceSize);
    }

    @Override
    public String getName() {
        return name;
    }

}
