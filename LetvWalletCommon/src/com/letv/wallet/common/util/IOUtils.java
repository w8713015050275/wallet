package com.letv.wallet.common.util;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by liuliang on 16-7-18.
 */
public class IOUtils {

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
            }
        }
    }
}
