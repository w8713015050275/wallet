package com.letv.walletbiz.base.activity;

import android.content.Intent;
import android.os.Bundle;

import com.letv.wallet.common.activity.AccountBaseActivity;
import com.letv.wallet.common.activity.BaseFragmentActivity;
import com.letv.walletbiz.update.UpdateHelper;

/**
 * Created by zhangzhiwei1 on 16-10-8.
 * support for apk upgrade
 */
public abstract class BaseWalletFragmentActivity extends AccountBaseActivity {

    protected UpdateHelper mUpdateHelper;
    @Override
    protected void onCreate(Bundle  bundle) {
        super.onCreate(bundle);
        mUpdateHelper = new UpdateHelper(this);
    }
    @Override
    protected void onStart() {
        super.onStart();
        if(canStartUpgradeService()) {
            mUpdateHelper.startUpgradeService();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mUpdateHelper.registerHideDialogReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mUpdateHelper.reset();
        mUpdateHelper.unRegisterHideDialogReceiver();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mUpdateHelper.handleNewIntent(intent);
    }

    public boolean canStartUpgradeService() {
        return true;
    }
}
