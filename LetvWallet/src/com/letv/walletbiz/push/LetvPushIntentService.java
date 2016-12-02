package com.letv.walletbiz.push;

import android.content.Context;
import android.text.TextUtils;

import com.letv.android.lcm.LetvPushBaseIntentService;
import com.letv.wallet.common.util.LogHelper;
import com.letv.walletbiz.push.beans.PushMessage;
import com.letv.walletbiz.push.notice.PushNoticeManager;
import com.letv.walletbiz.push.utils.PushMessageParser;

/**
 * Created by changjiajie on 16-5-30.
 */
public class LetvPushIntentService extends LetvPushBaseIntentService {

    private static final String TAG = LetvPushIntentService.class.getSimpleName();

    public LetvPushIntentService() {
        super(TAG);
    }

    public LetvPushIntentService(String name) {
        super(name);
    }

    protected void onMessage(Context context, String message) {
        LogHelper.d("[%s] push message == " + message, TAG);
        if (TextUtils.isEmpty(message)) {
            return;
        }

        PushMessage pushMessage = PushMessageParser.parsePushMessage(message);
        if (pushMessage != null) {
            int action = pushMessage.getAction();
            if (PushMessage.ACTION_CUSTOM == action) {

                // action为自定义行为
            } else {
                // 构造notification并发送通知
                if (!PushMessage.DEFAULT_ICON_URI.equals(pushMessage.getIconUri())) {
                    // 下载icon
                }
                PushNoticeManager.sendNotification(context, pushMessage);
            }
        }

    }


}