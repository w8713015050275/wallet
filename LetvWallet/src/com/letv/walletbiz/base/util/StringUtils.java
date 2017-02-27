package com.letv.walletbiz.base.util;

import android.content.Context;

import com.letv.wallet.common.BaseApplication;
import com.letv.walletbiz.R;

/**
 * Created by changjiajie on 17-1-19.
 */

public class StringUtils {

    public static String getFormat(int resourceId, Object content) {
        String str = "";
        try {
            str = String.format(BaseApplication.getApplication().getString(resourceId), String.valueOf(content));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

    public static String getPriceUnit(Object price) {
        return getFormat(R.string.price_unit, price);
    }

    public static String getPriceUnit(Context context, Object price) {
        return getFormat(R.string.price_unit, price);
    }

    public static String getDiscountPriceUnit(Object price) {
        return getFormat(R.string.price_unit_discount, price);
    }

    public static String getDiscountPriceUnit(Context context, Object price) {
        return getFormat(R.string.price_unit_discount, price);
    }
}
