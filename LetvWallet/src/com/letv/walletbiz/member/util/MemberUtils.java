package com.letv.walletbiz.member.util;

import android.content.Context;

import com.letv.walletbiz.R;

/**
 * Created by zhanghuancheng on 16-12-29.
 */

public class MemberUtils {
    private static final int MemberOrderStatusDisplayID[] = {
            R.string.member_order_status_to_pay,
            R.string.member_order_status_paid,
            R.string.member_order_refund_success,
            R.string.member_order_status_cancelled,
            R.string.member_order_refunding,
            R.string.member_order_success
    };

    public static String getOrderStatusStringByStatus(Context context, int status) {
        if (status > 0 && status <= MemberOrderStatusDisplayID.length) {
            return context.getString(MemberOrderStatusDisplayID[status - 1]);
        }
        return null;
    }
}
