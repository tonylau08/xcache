package com.igeeksky.xcache.core.extend;

/**
 * @author Patrick.Lau
 * @date 2017-03-07 07:03:36
 */
public interface KeyGenerator<T, R> {

    R convert(T t);

    T deConvert(R r);

    byte[] serialize(T t);

    T deSerialize(byte[] bytes);

}
