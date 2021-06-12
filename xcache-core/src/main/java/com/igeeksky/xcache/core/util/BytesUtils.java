/*
 * Copyright 2017 Tony.lau All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.igeeksky.xcache.core.util;

import java.util.Arrays;

/**
 * @author Patrick.Lau
 * @date 2017-03-01 05:09:16
 */
public class BytesUtils {

    public static final byte[] EMPTY_BYTES = new byte[0];

    public static final byte[] ONE_BYTES = new byte[]{1};

    public static boolean isEmpty(byte[] bytes) {
        return (null == bytes || bytes.length == 0);
    }

    public static byte[] merge(byte[] src1, byte[] src2) {
        if (null == src1 || src1.length == 0) return src2;
        if (null == src2 || src2.length == 0) return src1;

        int src1Len = src1.length;
        int src2Len = src2.length;
        byte[] dest = Arrays.copyOf(src1, src1Len + src2Len);
        System.arraycopy(src2, 0, dest, src1Len, src2Len);
        return dest;
    }

    private BytesUtils() {
    }
}
