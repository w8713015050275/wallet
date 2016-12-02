package com.letv.wallet.common.util;

import android.text.TextUtils;

import java.lang.reflect.Method;

/**
 * Created by liuliang on 16-11-21.
 */

public class SystemUtils {

    private static Class getSystemPropertiesClass() throws ClassNotFoundException {
        return Class.forName("android.os.SystemProperties");
    }

    public static String getSystemProperties(String key, String defaultValue) {
        if (!TextUtils.isEmpty(key)) {
            try {
                Class clazz = getSystemPropertiesClass();
                Method method = clazz.getMethod("get", String.class, String.class);
                return (String) method.invoke(null, key, defaultValue);
            } catch (Exception e) {
                LogHelper.e(e.toString());
            }
        }
        return defaultValue;
    }

    public static boolean getSystemBooleanProperties(String key, boolean defaultValue) {
        if (!TextUtils.isEmpty(key)) {
            try {
                Class clazz = getSystemPropertiesClass();
                Method method = clazz.getMethod("getBoolean", String.class, boolean.class);
                return (Boolean) method.invoke(null, key, defaultValue);
            } catch (Exception e) {
                LogHelper.e(e.toString());
            }
        }
        return defaultValue;
    }
}
