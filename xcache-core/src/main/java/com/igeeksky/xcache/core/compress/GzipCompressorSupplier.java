package com.igeeksky.xcache.core.compress;

/**
 * @author Patrick.Lau
 * @date 2021-06-23
 */
public class GzipCompressorSupplier implements CompressorSupplier {

    @Override
    public Compressor get() {
        return GzipCompressor.COMPRESSOR;
    }
}
