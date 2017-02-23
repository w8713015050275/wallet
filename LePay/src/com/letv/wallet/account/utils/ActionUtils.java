package com.letv.wallet.account.utils;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.letv.wallet.base.util.WalletConstant;
import com.letv.wallet.common.util.CommonConstants;

/**
 * Created by lijunying on 17-2-17.
 */

public class ActionUtils {

    public static Intent newIntent(Context fromContext, Class<?> toCls, String from) {
        Intent intent = new Intent(fromContext, toCls);
        if (!TextUtils.isEmpty(from)) {
            intent.putExtra(CommonConstants.EXTRA_FROM, from);
        }
        return intent;
    }

    public static String getFromExtra(Intent intent) {
        if (intent != null) {
            return intent.getStringExtra(WalletConstant.EXTRA_FROM);
        }
        return null;
    }


}
