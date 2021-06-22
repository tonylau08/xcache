package com.igeeksky.xcache.core.compress;

import java.io.IOException;

/**
 * @author Patrick.Lau
 * @date 2021-06-22
 */
public interface Compressor {

    byte[] compress(byte[] source) throws IOException;

    byte[] decompress(byte[] source) throws IOException;

}
