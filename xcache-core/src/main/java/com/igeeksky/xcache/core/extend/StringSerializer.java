package com.igeeksky.xcache.core.extend;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author: Patrick.Lau
 * @date: 2021-06-20
 */
public class StringSerializer implements Serializer<String> {

    public static final StringSerializer UTF_8 = new StringSerializer(StandardCharsets.UTF_8);

    private Charset charset;

    public StringSerializer() {
        this(StandardCharsets.UTF_8);
    }

    public StringSerializer(Charset charset) {
        this.charset = charset;
    }

    @Override
    public byte[] serialize(String source) {
        return source.getBytes(charset);
    }

    @Override
    public String deserialize(byte[] bytes) {
        return new String(bytes, charset);
    }
}
