package com.letv.wallet.common.util;

import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import com.letv.wallet.common.BaseApplication;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Created by liuliang on 17-2-16.
 */

public class RootCheckHelper {


    private static Boolean isRoot;

    private static final String[] SU_PATHS ={
            "/data/local/",
            "/data/local/bin/",
            "/data/local/xbin/",
            "/sbin/",
            "/system/bin/",
            "/system/bin/.ext/",
            "/system/bin/failsafe/",
            "/system/sd/xbin/",
            "/system/usr/we-need-root/",
            "/system/xbin/"
    };

    private static final String[] ROOT_APPS_PACKAGES = {
            "com.noshufou.android.su",
            "com.noshufou.android.su.elite",
            "eu.chainfire.supersu",
            "com.koushikdutta.superuser",
            "com.thirdparty.superuser",
            "com.yellowes.su",

    };

    public static final String[] PATH_SHOULD_NOT_WRITABLE = {
            "/system",
            "/system/bin",
            "/system/sbin",
            "/system/xbin",
            "/vendor/bin",
            "/sbin",
            "/etc",
    };

    public static boolean isRoot() {
        if (isRoot == null) {
            isRoot = checkSuExists() || checkSuBinary() || checkBusyBoxBinary() || hasTestKeys()
                    || checkForDangerousProps() || isSelinuxFlagInEnabled() || checkRootManagementApps()
                    || checkForRWPaths();
        }
        return isRoot;
    }

    private static boolean checkSuExists() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(new String[] { "/system/xbin/which", "su" });
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            return in.readLine() != null;
        } catch (Throwable t) {
            return false;
        } finally {
            if (process != null) process.destroy();
        }
    }

    private static boolean checkSuBinary() {
        return checkBinary("su");
    }

    private static boolean checkBusyBoxBinary() {
        return checkBinary("busybox");
    }

    private static boolean hasTestKeys() {
        String buildTags = Build.TAGS;
        return buildTags != null && buildTags.contains("test-keys");
    }

    private static boolean checkForDangerousProps() {
        final Map<String, String> dangerousProps = new HashMap<String, String>();
        dangerousProps.put("ro.debuggable", "1");
        dangerousProps.put("ro.secure", "0");

        boolean result = false;
        String value;
        for (String key : dangerousProps.keySet()) {
            value = getPropVal(key);
            if (!TextUtils.isEmpty(value) && value.contains(dangerousProps.get(key))) {
                result = true;
                break;
            }
        }
        return result;
    }

    private static boolean isSelinuxFlagInEnabled() {
        String selinux = null;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            selinux = (String) get.invoke(c, "ro.build.selinux");
        } catch (Exception ignored) {

        }

        return "1".equals(selinux) ? true : false;
    }

    private static boolean checkRootManagementApps() {
        return isAnyPackageInstalled(ROOT_APPS_PACKAGES);
    }

    private static boolean checkForRWPaths() {

        boolean result = false;

        String[] lines = mountReader();
        for (String line : lines) {

            String[] args = line.split(" ");

            if (args.length < 4){
                continue;
            }

            String mountPoint = args[1];
            String mountOptions = args[3];

            for(String pathToCheck: PATH_SHOULD_NOT_WRITABLE) {
                if (mountPoint.equalsIgnoreCase(pathToCheck)) {
                    for (String option : mountOptions.split(",")){
                        if (option.equalsIgnoreCase("rw")){
                            result = true;
                            break;
                        }
                    }
                }
            }
        }

        return result;
    }

    private static boolean checkBinary(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return false;
        }
        boolean result = false;
        File file;
        for (String path : SU_PATHS) {
            file = new File(path + fileName);
            if (file.exists()) {
                result = true;
                break;
            }
        }
        return result;
    }

    private static String getPropVal(String prop) {
        InputStream inputstream = null;
        try {
            inputstream = Runtime.getRuntime().exec("getprop " + prop).getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String propval = "";
        try {
            propval = new Scanner(inputstream).useDelimiter("\\A").next();

        } catch (NoSuchElementException e) {
        }

        return propval;
    }

    private static boolean isAnyPackageInstalled(String[] packages){
        if (packages == null) {
            return false;
        }
        boolean result = false;
        PackageManager pm = BaseApplication.getApplication().getPackageManager();
        for (String packageName : packages) {
            try {
                pm.getPackageInfo(packageName, 0);
                result = true;
                break;
            } catch (PackageManager.NameNotFoundException e) {
            }
        }
        return result;
    }

    private static String[] mountReader() {
        InputStream inputstream = null;
        try {
            inputstream = Runtime.getRuntime().exec("mount").getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // If input steam is null, we can't read the file, so return null
        if (inputstream == null) return null;

        String propval = "";
        try {
            propval = new Scanner(inputstream).useDelimiter("\\A").next();
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        }

        return propval.split("\n");
    }
}
