package com.letv.leui.common.recommend.widget;

import android.content.Context;
import com.letv.leui.common.recommend.utils.AppUtil;
import com.letv.leui.common.recommend.utils.FeatureUtils;

import java.util.Set;

/**
 * Created by zhangjiahao on 15-7-8.
 */
public class LeRecommendWordsType {
    public static final String STAR = "明星";
    public static final String MOVIE = "影视";
    public static final String PLAYER = "球员";
    public static final String TEAM = "球队";
    public static final String BRAND = "品牌";
    public static final String REGION = "地域";

    public static void excludeModuleByType(Context context, String type, Set<LeRecommendType> mExcludeTypes) {
        String packageName = context.getPackageName();
        if (packageName.equals(AppUtil.MUSIC_PACKAGE_NAME)) {
            excludeMusicModuleByType(type, mExcludeTypes);
        }else if (packageName.equals(AppUtil.CALENDAR_PACKAGE_NAME)) {
            excludeCalendarModuleByType(type, mExcludeTypes);
        }else if (packageName.equals(AppUtil.WALLPAPER_PACKAGE_NAME)) {
            excludeWallpaperModuleByType(type, mExcludeTypes);
        }

        // [+EUI][REQ][LEUI-14330][zhangjiahao] add 2016.04.13
        // block online music
        if (FeatureUtils.isOnlineMusicBlocked(context)) {
            mExcludeTypes.add(LeRecommendType.MUSIC);
            mExcludeTypes.add(LeRecommendType.ALBUM);
        }
        // [-EUI]
    }

    private static void excludeMusicModuleByType(String type, Set<LeRecommendType> mExcludeTypes) {
        mExcludeTypes.add(LeRecommendType.ARTISTS);
        mExcludeTypes.add(LeRecommendType.MUSIC);
        mExcludeTypes.add(LeRecommendType.ALBUM);
    }

    private static void excludeCalendarModuleByType(String type, Set<LeRecommendType> mExcludeTypes) {
        if (type.equals(STAR)) {
            //no exclude
        }else if (type.equals(MOVIE)) {
            //no exclude
        }else if (type.equals(PLAYER)) {
            //no exclude
        }else if (type.equals(TEAM)) {
            //no exclude
        }else if (type.equals(BRAND)) {
            //no exclude
        }else if (type.equals(REGION)) {
            //no exclude
        }
    }


    private static void excludeWallpaperModuleByType(String type, Set<LeRecommendType> mExcludeTypes) {
        mExcludeTypes.add(LeRecommendType.WALLPAPER);
    }
}
