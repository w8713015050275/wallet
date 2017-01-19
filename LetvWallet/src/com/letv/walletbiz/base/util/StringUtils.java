package com.letv.walletbiz.base.util;

import android.content.Context;

import com.letv.walletbiz.R;

/**
 * Created by changjiajie on 17-1-19.
 */

public class StringUtils {

    public static String getPriceUnit(Context context, Object price) {
        String priceStr = "";
        try {
            priceStr = String.format(context.getString(R.string.price_unit), String.valueOf(price));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return priceStr;
    }
}
