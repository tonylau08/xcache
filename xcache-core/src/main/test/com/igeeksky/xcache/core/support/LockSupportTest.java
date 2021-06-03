package com.igeeksky.xcache.core.extend;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Patrick.Lau
 * @date 2020-12-14
 */
public class LockSupportTest {

    @Test
    public void getLockTest() {

        List<String> keys = new ArrayList<>();
        for (int i = 0; i < 128; i++) {
            keys.add("a" + i);
        }

        LockSupport lockSupport = new LockSupport(256);
        Lock lock = lockSupport.getLockByKeys(keys);
    }

}
