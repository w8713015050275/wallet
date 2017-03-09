package com.letv.wallet;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import com.letv.tracker2.agnes.Agnes;
import com.letv.tracker2.enums.Area;
import com.letv.tracker2.enums.HwType;
import com.letv.wallet.base.util.WalletConstant;
import com.letv.wallet.common.BaseApplication;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.EnvUtil;
import com.letv.wallet.common.util.LogHelper;

import org.xutils.xmain;

/**
 * Created by changjiajie on 16-10-11.
 */
public class PayApplication extends BaseApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        initEnv();
        getUToken();
        xmain.Ext.init(this);
        xmain.Ext.setDebug(true);
        Agnes.getInstance(HwType.PHONE_LETV, Area.CN).setContext(this);
    }

    @Override
    public String getAppUA() {
        return WalletConstant.PAY_UA_NAME + " " + getAppVersion();
    }

    /* Try to get Letv account Token Since Start*/
    private void getUToken() {
        AccountHelper accountHelper = AccountHelper.getInstance();
        if (accountHelper.isLogin(this)){
            accountHelper.getTokenASync(this);
        }
    }

    private void initEnv(){
        Context targetAPPContext = null;
        try {
            targetAPPContext = createPackageContext("com.letv.wallet.evmsettingapp", CONTEXT_IGNORE_SECURITY);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if(targetAPPContext!=null){
            SharedPreferences mPreferences = targetAPPContext.getSharedPreferences("wallet_pay", Context.MODE_PRIVATE);
            if(mPreferences!=null){
                boolean result = mPreferences.getBoolean("wallet", true);
                LogHelper.d("PayApplication Env is "+ result);
                EnvUtil.getInstance().setLePayTest(result);
            }
        }
    }
}
