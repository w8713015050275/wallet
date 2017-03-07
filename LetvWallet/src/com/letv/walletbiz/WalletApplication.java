package com.letv.walletbiz;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import com.letv.tracker.agnes.Agnes;
import com.letv.wallet.common.BaseApplication;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.EnvUtil;
import com.letv.wallet.common.util.LogHelper;
import com.letv.walletbiz.base.util.WalletConstant;
import com.letv.walletbiz.push.PushRegisterService;

import org.xutils.xmain;

import com.letv.walletbiz.update.UpdateConstant;
import com.letv.walletbiz.update.util.CrashHandler;
import com.letv.walletbiz.update.util.UpdateUtil;

/**
 * Created by linquan on 15-12-25.
 */
public class WalletApplication extends BaseApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        initEnv();
        getUToken();
        xmain.Ext.init(this);
        xmain.Ext.setDebug(true);
        Agnes.getInstance().setContext(this);
        initPushService();

        UpdateUtil.mIsStartedNewly = true;
        CrashHandler.getInstance().init(this);
        UpdateConstant.UPGRADE_DOWNLOAD_APK_SAVE_PATH = getExternalCacheDir().getPath();
    }

    @Override
    public String getAppUA() {
        return WalletConstant.WALLET_UA_NAME + " " + getAppVersion();
    }

    /* Try to get Letv account Token Since Start*/
    private void getUToken() {
        AccountHelper accountHelper = AccountHelper.getInstance();
        if (accountHelper.isLogin(this)){
            accountHelper.getTokenASync(this);
        }
    }
    private void initPushService() {
        PushRegisterService.initPush();
    }

    private void initEnv() {
        Context targetAPPContext = null;
        try {
            targetAPPContext = createPackageContext("com.letv.wallet.evmsettingapp", CONTEXT_IGNORE_SECURITY);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (targetAPPContext != null) {
            SharedPreferences mPreferences = targetAPPContext.getSharedPreferences("wallet_pay", Context.MODE_PRIVATE);
            if (mPreferences != null) {
                boolean result = mPreferences.getBoolean("walletbiz", true);
                LogHelper.d("WalletApplication Env is " + result);
                EnvUtil.getInstance().setWalletTest(result);
            }
        }
    }
}
