package com.letv.wallet.account.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.letv.wallet.account.aidl.v1.AccountConstant;
import com.letv.wallet.common.util.LogHelper;

/**
 * Created by lijunying on 17-1-17.
 */

public class AccountService extends Service {
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        LogHelper.e("onBind");
        int version = intent.getIntExtra(AccountConstant.EXTRA_AIDL_VERSION, 0);
        switch (version) {
            case 1:
                 return new AccountServiceImpV1() ;

            default:
                return null;

        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

}
