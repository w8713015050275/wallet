package com.letv.leui.common.recommend.utils;

import android.content.Context;

/**
 * Created by zhangjiahao on 16-4-12.
 */
public class FeatureUtils {

    public static boolean isOnlineMusicBlocked(Context context) {
        return context.getPackageManager().hasSystemFeature("music.block_online_music");
    }

}
