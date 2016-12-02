package com.letv.walletbiz.push.utils;

import android.text.TextUtils;

import com.google.gson.reflect.TypeToken;
import com.letv.wallet.common.util.LogHelper;
import com.letv.wallet.common.util.ParseHelper;
import com.letv.walletbiz.push.beans.PushMessage;

/**
 * Created by lijujying on 16-7-9.
 */
public class PushMessageParser {
    private static final String TAG = PushMessageParser.class.getSimpleName();

    private int action;
    private String web_uri;
    private String intent_val;
    private String custom_val;
    private String title;
    private String content;
    private String icon_uri;
    private String intent_uri;
    private String msgid;

    public static PushMessage parsePushMessage(String result) {
        if (!TextUtils.isEmpty(result)) {
            TypeToken typeToken = new TypeToken<PushMessageParser>() {
            };
            PushMessageParser parser = ParseHelper.parseByGson(result, typeToken.getType());
            if(parser != null){
                parser.icon_uri = TextUtils.isEmpty(parser.icon_uri) ? PushMessage.DEFAULT_ICON_URI : parser.icon_uri;
                PushMessage pushMessage = new PushMessage(parser.msgid, parser.action, parser.web_uri, parser.intent_val, parser.custom_val, parser.title, parser.content, parser.icon_uri, parser.intent_uri);
                LogHelper.d("[%S] push message = " + pushMessage.toString(), TAG);
                return pushMessage;
            }else{
                LogHelper.e("[%S] parser push message is null, result = " + result, TAG);
            }
        }
        return null;
    }
}
