package com.letv.walletbiz.push.notice;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.letv.wallet.common.util.AppUtils;
import com.letv.wallet.common.util.CommonConstants;
import com.letv.wallet.common.util.LogHelper;
import com.letv.walletbiz.MainActivity;
import com.letv.walletbiz.base.util.Action;
import com.letv.walletbiz.base.util.WalletConstant;
import com.letv.walletbiz.push.PushWebActivity;
import com.letv.walletbiz.push.beans.PushMessage;

/**
 * Created by lijujying on 16-7-14.
 */
public class NoticeActionService  extends IntentService {
    private static final String TAG = NoticeActionService.class.getSimpleName();
    private static final int INTENT_URI = 0;
    private static final int INTENT_PACKAGE_WITH_ACTION = 1;
    private Intent mIntent ;

    public NoticeActionService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        PushMessage pushMessage = (PushMessage) intent.getSerializableExtra("pushMessage");
        if (pushMessage != null && buildContentIntent(pushMessage)) {
            try {
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED| Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT );
                startActivity(mIntent);
            } catch (Exception e) {
                LogHelper.d("[%s] onHandleIntent = " + e.getMessage());
            }

        }
    }

    private boolean buildContentIntent(PushMessage pushMessage) {
        int action = pushMessage.getAction();
        mIntent = null;
        Uri uri = null;
        switch (action) {
            case PushMessage.ACTION_OPEN_APP: // 打开app
                mIntent = new Intent(this, MainActivity.class);
                mIntent.putExtra(WalletConstant.EXTRA_FROM, Action.EVENT_PROP_FROM_PUSH);
                Action.uploadPushExpose(pushMessage.getMsgId(), Action.EVENT_PROP_TO_HOME);
                break;

            case PushMessage.ACTION_OPEN_WEB:
                String uriStr = pushMessage.getWebUri();
                if (TextUtils.isEmpty(uriStr)) {
                    return false;
                }
                Action.uploadPushExpose(pushMessage.getMsgId());
                mIntent = new Intent(this, PushWebActivity.class);
                mIntent.putExtra(WalletConstant.EXTRA_FROM, Action.EVENT_PROP_FROM_PUSH);
                mIntent.putExtra(CommonConstants.EXTRA_URL, uriStr);
                break;

            case PushMessage.ACTION_OPEN_ACTIVITY:
                String intentUri = pushMessage.getIntentUri();
                if (TextUtils.isEmpty(intentUri)) {
                    return false;
                }
                Action.uploadPushExpose(pushMessage.getMsgId());
                int intentType = getIntentUriType(intentUri);

                if (intentType == INTENT_URI) {
                    uri = Uri.parse(intentUri);
                    if(!TextUtils.isEmpty(uri.getQuery()) && !TextUtils.isEmpty(pushMessage.getIntentVal())){
                        uri = Uri.parse(intentUri + pushMessage.getIntentVal());
                    }
                    uri = uri.buildUpon().appendQueryParameter(WalletConstant.EXTRA_FROM, Action.EVENT_PROP_FROM_PUSH).build();
                    mIntent = new Intent(Intent.ACTION_VIEW, uri);
                } else {
                    String[] array = getPacageAndAction(intentUri);
                    if (array[0] != null && array[1] != null) {
                        if (AppUtils.isAppInstalled(this, array[0])) {
                            mIntent = new Intent(array[1]);
                        } else {
                            mIntent = AppUtils.getAppStoreIntent(array[0]);
                        }
                    }
                    if (mIntent != null) {
                        mIntent.putExtra(WalletConstant.EXTRA_FROM, Action.EVENT_PROP_FROM_PUSH);
                    }
                }
                break;
        }

        return mIntent != null;
    }

    public static String[] getPacageAndAction(String packWithAction) {
        if (TextUtils.isEmpty(packWithAction)) {
            return new String[2];
        }
        int index = packWithAction.indexOf('@');
        String packname = null;
        String action = null;
        if (index > 0) {
            packname = packWithAction.substring(0, index);
            action = packWithAction.substring(index + 1);
        }
        return new String[]{packname, action};
    }

    private  static int getIntentUriType(String intentUri){
        int index = intentUri.indexOf('@');
        if(index > 0)
            return INTENT_PACKAGE_WITH_ACTION;

        return INTENT_URI;
    }
}
