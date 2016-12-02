package com.letv.walletbiz.push;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.letv.android.lcm.LetvPushWakefulReceiver;

/**
 * Created by changjiajie on 16-5-30.
 */
public class LePushMessageReceiver extends LetvPushWakefulReceiver {
    private static final String TAG = LePushMessageReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if(context == null || intent == null){
            return;
        }
        ComponentName comp = new ComponentName(context.getPackageName(), LetvPushIntentService.class.getName());
        startWakefulService(context, (intent.setComponent(comp)));
    }
}