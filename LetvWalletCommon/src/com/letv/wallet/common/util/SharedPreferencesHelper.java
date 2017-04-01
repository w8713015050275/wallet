package com.letv.wallet.common.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.letv.wallet.common.BaseApplication;

import java.util.Set;

/**
 * Created by liuliang on 16-1-6.
 */
public class SharedPreferencesHelper {

    public static final String PREFERENCE_NAME = "wallet_preferences";

    public static SharedPreferences getSharePreferences() {
        return BaseApplication.getApplication().getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    public static SharedPreferences getUserIdPreferences(String packName) {
        if (!TextUtils.isEmpty(packName)) {
            try {
                Context context = BaseApplication.getApplication().createPackageContext(packName, Context.CONTEXT_IGNORE_SECURITY);
                if (context != null) {
                     return  context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    public static String getString(String key, @Nullable String defValue) {
        return getSharePreferences().getString(key, defValue);
    }

    public static Set<String> getStringSet(String key, @Nullable Set<String> defValues) {
        return getSharePreferences().getStringSet(key, defValues);
    }

    public static int getInt(String key, int defValue) {
        return getSharePreferences().getInt(key, defValue);
    }

    public static long getLong(String key, long defValue) {
        return getSharePreferences().getLong(key, defValue);
    }

    public static float getFloat(String key, float defValue) {
        return getSharePreferences().getFloat(key, defValue);
    }

    public static boolean getBoolean(String key, boolean defValue) {
        return getSharePreferences().getBoolean(key, defValue);
    }

    public static boolean contains(String key) {
        return getSharePreferences().contains(key);
    }

    public static void putString(String key, @Nullable String value) {
        getSharePreferences().edit().putString(key, value).apply();
    }

    public static void putStringSet(String key, @Nullable Set<String> values) {
        getSharePreferences().edit().putStringSet(key, values).apply();
    }

    public static void putInt(String key, int value) {
        getSharePreferences().edit().putInt(key, value).apply();
    }

    public static void putLong(String key, long value) {
        getSharePreferences().edit().putLong(key, value).apply();
    }

    public static void putFloat(String key, float value) {
        getSharePreferences().edit().putFloat(key, value).apply();
    }

    public static void putBoolean(String key, boolean value) {
        getSharePreferences().edit().putBoolean(key, value).apply();
    }

    public static void remove(String key) {
        getSharePreferences().edit().remove(key).apply();
    }

    public static void clear() {
        getSharePreferences().edit().clear().apply();
    }

    public static void registerOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        getSharePreferences().registerOnSharedPreferenceChangeListener(listener);
    }

    public static void unregisterOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        getSharePreferences().unregisterOnSharedPreferenceChangeListener(listener);
    }
}
