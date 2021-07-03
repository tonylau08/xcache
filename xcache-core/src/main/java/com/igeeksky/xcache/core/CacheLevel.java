package com.igeeksky.xcache.core;

import com.igeeksky.xcache.core.util.StringUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Patrick.Lau
 * @date 2021-06-25
 */
public enum CacheLevel {

    /**
     * L0为组合缓存
     */
    L0,

    /**
     * L1~L9为用户定义缓存
     */
    L1,

    L2,

    L3,

    L4,

    L5,

    L6,

    L7,

    L8,

    L9;

    private static final Map<String, CacheLevel> SUPPORTED_CACHE_LEVEL;

    static {
        Map<String, CacheLevel> cacheLevelMap = new LinkedHashMap<>();
        CacheLevel[] cacheLevels = CacheLevel.values();
        for (CacheLevel cacheLevel : cacheLevels) {
            cacheLevelMap.put(cacheLevel.name().toUpperCase(), cacheLevel);
        }
        SUPPORTED_CACHE_LEVEL = Collections.unmodifiableMap(cacheLevelMap);
    }

    public static CacheLevel getByName(String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        String upperCase = name.toUpperCase();
        return SUPPORTED_CACHE_LEVEL.get(upperCase);
    }

}
