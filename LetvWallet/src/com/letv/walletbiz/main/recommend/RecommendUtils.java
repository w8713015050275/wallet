package com.letv.walletbiz.main.recommend;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.letv.wallet.common.util.CommonConstants;
import com.letv.wallet.common.util.LogHelper;
import com.letv.walletbiz.main.WalletMainWebActivity;

/**
 * Created by liuliang on 17-2-13.
 */

public class RecommendUtils {

    public static boolean launchUrl(Context context, String url) {
        if (context == null || TextUtils.isEmpty(url)) {
            return false;
        }
        String decodeUrl = Uri.decode(url);
        Intent intent = null;
        if (decodeUrl.startsWith("http")) {
            intent = new Intent(context, WalletMainWebActivity.class);
            intent.putExtra(CommonConstants.EXTRA_URL, decodeUrl);
        } else if (decodeUrl.startsWith("intent:")) {
            try {
                intent = Intent.parseUri(decodeUrl, Intent.URI_INTENT_SCHEME);
            } catch (Exception e) {
                LogHelper.d(e.toString());
                intent = null;
            }
        } else {
            try {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(decodeUrl));
            } catch (Exception e) {
                LogHelper.d(e.toString());
                intent = null;
            }
        }
        if (intent == null) {
            return false;
        }
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            LogHelper.e(e);
            return false;
        }
        return true;
    }
}
