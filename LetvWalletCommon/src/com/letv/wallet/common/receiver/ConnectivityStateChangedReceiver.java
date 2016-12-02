package com.letv.wallet.common.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.letv.wallet.common.util.LogHelper;
import com.letv.wallet.common.util.NetworkHelper;

/**
 * Created by linquan on 16-1-6.
 */
public class ConnectivityStateChangedReceiver extends BroadcastReceiver {
    public final static String TAG = "ConnectivityStateChangedReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        NetworkHelper networkHelper = NetworkHelper.getInstance();
        LogHelper.w("[%s] onReceive ", TAG);

        if (networkHelper.isNetworkAvailable()) {
            if (networkHelper.containsCallBack()) {
                networkHelper.excuteCallBack();
            }
        }
    }

}
