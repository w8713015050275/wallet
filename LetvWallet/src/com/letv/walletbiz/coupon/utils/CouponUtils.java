package com.letv.walletbiz.coupon.utils;

import android.content.Context;
import android.widget.Toast;

import com.letv.wallet.common.util.DateUtils;
import com.letv.walletbiz.R;
import com.letv.walletbiz.coupon.beans.BaseCoupon;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by lijunying on 16-4-20.
 */
public class CouponUtils {
    public  static void showToast(Context context,int resId){
        if (context == null) {
            return;
        }
        Toast.makeText(context.getApplicationContext(), resId, Toast.LENGTH_SHORT).show();
    }

    public  static  void showToast(Context context, CharSequence text){
        if (context == null) {
            return;
        }
        Toast.makeText(context.getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    public static String getCouponExpiryDateStr(Context context, BaseCoupon coupon) {
        if (coupon == null || context == null) {
            return "";
        }
        SimpleDateFormat sdf = DateUtils.getSimpleDateFormatByLocale();
        if (coupon.start_time * 1000 <= System.currentTimeMillis()) {
            return sdf.format(new Date(coupon.end_time * 1000));
        } else {
            return sdf.format(new Date(coupon.start_time * 1000)) + context.getString(R.string.coupon_validite_period_to)
                    + sdf.format(new Date(coupon.end_time * 1000));
        }
    }
}
