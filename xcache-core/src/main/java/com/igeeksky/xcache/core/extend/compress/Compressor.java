package com.igeeksky.xcache.core.extend.compress;

import java.io.IOException;

/**
 * @author Patrick.Lau
 * @date 2021-06-22
 */
public interface Compressor {

    /**
     * 压缩
     * @param source
     * @return
     * @throws IOException
     */
    byte[] compress(byte[] source) throws IOException;

    /**
     * 解压缩
     * @return
     * @throws IOException
     */
    byte[] decompress(byte[] source) throws IOException;

}
