package com.letv.wallet.common.util;

import android.app.Application;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import com.letv.wallet.common.BaseApplication;

/**
 * Created by liuliang on 16-7-18.
 */
public class DensityUtils {

    public static float dip2px(float dip) {
        DisplayMetrics metrics = BaseApplication.getApplication().getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, metrics);
    }

    public static float px2dip(float px) {
        DisplayMetrics metrics = BaseApplication.getApplication().getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, px, metrics);
    }

    public static int getScreenWidth() {
        Application application = BaseApplication.getApplication();
        int screenWidth = -1;
        if (application != null) {
            screenWidth = application.getResources().getDisplayMetrics().widthPixels;
        }
        return screenWidth;
    }

    public static int getScreenHeight() {
        Application application = BaseApplication.getApplication();
        int screenHeight = -1;
        if (application != null) {
            screenHeight = application.getResources().getDisplayMetrics().heightPixels;
        }
        return screenHeight;
    }
}
