package com.igeeksky.xcache.core.compress;

import com.igeeksky.xcache.core.extend.compress.GzipCompressor;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Random;

/**
 * @author Patrick.Lau
 * @date 2021-06-23
 */
class GzipCompressorTest {

    int size = 100;

    @Test
    void compress() throws IOException {
        byte[] source = loadFile();
        System.out.println("File length: " + source.length);
        GzipCompressor gzipCompressor = new GzipCompressor();
        long start = System.currentTimeMillis();
        byte[] compress = null;
        for (int i = 0; i < size; i++) {
            compress = gzipCompressor.compress(source);
            if ((i & 63) == 0) {
                System.out.println(compress.length);
            }
        }
        long end = System.currentTimeMillis();
        System.out.println(end - start);

        byte[] decompress = gzipCompressor.decompress(compress);
        System.out.println("decompress.length:" + decompress.length);

    }

    private byte[] loadFile() throws IOException {
        byte[] out = new byte[1024 * 1024];
        Random random = new Random();
        random.nextBytes(out);
        return out;
    }

}