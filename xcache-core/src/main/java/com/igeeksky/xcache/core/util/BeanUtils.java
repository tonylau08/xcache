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

package com.igeeksky.xcache.core.util;


import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Patrick.Lau
 * @blog: https://my.oschina.net/xcafe
 * @createTime 2017-03-02 18:28:37
 */
public class BeanUtils {

    public static Object getBeanProperty(Object bean, String fieldName) {
        Objects.requireNonNull(bean, "bean must not be null");

        try {
            Field f = bean.getClass().getDeclaredField(fieldName);
            if (null != f) {
                f.setAccessible(true);
                return f.get(bean);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> List<T> getBeansProperty(Object[] list, String fieldName, Class<T> type) {
        Objects.requireNonNull(list, "beanList must not be null");

        int length = list.length;
        if (length > 0) {
            List<T> ids = new ArrayList<T>();
            for (int i = 0; i < length; i++) {
                ids.add((T) getBeanProperty(list[i], fieldName));
            }
            return ids;
        }
        return null;
    }

}
