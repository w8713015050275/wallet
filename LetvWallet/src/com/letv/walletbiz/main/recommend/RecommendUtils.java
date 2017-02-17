package com.letv.walletbiz.main.recommend;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;

import com.letv.tracker.enums.EventType;
import com.letv.tracker.enums.Key;
import com.letv.wallet.common.util.CommonConstants;
import com.letv.wallet.common.util.LogHelper;
import com.letv.walletbiz.base.util.Action;
import com.letv.walletbiz.base.util.WalletConstant;
import com.letv.walletbiz.main.WalletMainWebActivity;

import java.util.HashMap;

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
            intent.putExtra(WalletConstant.EXTRA_FROM,Action.EVENT_PROP_FROM_CARD);
            context.startActivity(intent);
        } catch (Exception e) {
            LogHelper.e(e);
            return false;
        }
        return true;
    }

    public static void uploadCardClick(View view) {
        if (view == null) {
            return;
        }
        Object type = view.getTag();
        HashMap<String, Object> map = new HashMap<String, Object>();
        if (type != null) {
            map.put(Key.Content.toString(), String.valueOf(type));
        }
        Action.uploadCustom(EventType.Click, Action.RECOMMEND_CARDS_CLICK, map);
    }
}
