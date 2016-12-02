package com.letv.leui.common.recommend.utils;

import java.util.*;

/**
 * Created by dupengtao on 15-1-29.
 */
public class AppUtil {
    public static Map<String, Boolean> appInstallMap = Collections.synchronizedMap(new HashMap<String, Boolean>());
    public static List<String> appPackageNames = new ArrayList<String>();
    public static String WALLPAPER_PACKAGE_NAME = "com.letv.android.wallpaperonline";
    public static String VIDEO_PACKAGE_NAME = "com.letv.android.client";
    public static String SPORTS_PACKAGE_NAME = "com.letv.android.sports";
    public static String MUSIC_PACKAGE_NAME = "com.android.music";
    public static String CALENDAR_PACKAGE_NAME = "com.android.calendar";
    public static String WEI_PACKAGE_NAME = "com.sina.weibo";
    public static String LESO_PACKAGE_NAME = "com.letv.lesophoneclient";

    static {
        appPackageNames.add(WALLPAPER_PACKAGE_NAME);
        appPackageNames.add(VIDEO_PACKAGE_NAME);
        appPackageNames.add(SPORTS_PACKAGE_NAME);
        appPackageNames.add(MUSIC_PACKAGE_NAME);
        appPackageNames.add(CALENDAR_PACKAGE_NAME);
        appPackageNames.add(WEI_PACKAGE_NAME);
        appPackageNames.add(LESO_PACKAGE_NAME);
    }
}
