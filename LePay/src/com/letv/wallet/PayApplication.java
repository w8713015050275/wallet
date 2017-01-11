package com.letv.wallet;

import com.letv.tracker.agnes.Agnes;
import com.letv.wallet.base.util.WalletConstant;
import com.letv.wallet.common.BaseApplication;
import com.letv.wallet.common.util.AccountHelper;

import org.xutils.xmain;

/**
 * Created by changjiajie on 16-10-11.
 */
public class PayApplication extends BaseApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        getUToken();
        xmain.Ext.init(this);
        xmain.Ext.setDebug(true);
        Agnes.getInstance().setContext(this);
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
}
