package com.igeeksky.xcache.core.exception;

/**
 * @author Patrick.Lau
 * @date 2020-12-11
 */
public class SerializationFailedException extends RuntimeException {


    public SerializationFailedException(String msg, Exception e) {
        super(msg, e);
    }
}
