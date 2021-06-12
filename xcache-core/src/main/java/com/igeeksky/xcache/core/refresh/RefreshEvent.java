package com.igeeksky.xcache.core.refresh;

import com.igeeksky.xcache.core.Event;

/**
 * @author Patrick.Lau
 * @date 2021-06-05
 */
public class RefreshEvent implements Event {

    private String src;

    private String name;

    private byte[] key;

    private byte[] value;

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getKey() {
        return key;
    }

    public void setKey(byte[] key) {
        this.key = key;
    }

    public byte[] getValue() {
        return value;
    }

    public void setValue(byte[] value) {
        this.value = value;
    }
}
