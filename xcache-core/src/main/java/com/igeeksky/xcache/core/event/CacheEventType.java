package com.igeeksky.xcache.core.event;

/**
 * @author Patrick.Lau
 * @date 2021-06-25
 */
public enum CacheEventType {

    /**
     * 通过Key删除缓存
     */
    REMOVE,

    /**
     * 通过loader重新加载数据
     */
    RELOAD

}
