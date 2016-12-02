package com.letv.wallet.wxapi;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.letv.lepaysdk.wxpay.WXPay;
import com.letv.wallet.common.util.LogHelper;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
/**
 * LePaySDK回调类
 */
public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler{
	/**微信APP_ID 替换为你的应用从官方网站申请到的合法appId*/
	private static final String APP_ID = "wxfecf79ae6c454998";

	private IWXAPI api;
    private WXPay wxPay;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wxPay=WXPay.getInstance(this);
        api = WXAPIFactory.createWXAPI(this, APP_ID);
        api.handleIntent(getIntent(), this);
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
    }
    @Override
    public void onReq(BaseReq req) {
    }
    @Override
    public void onResp(BaseResp resp) {
        wxPay.setResp(resp);
        LogHelper.d("%S resp: %d", WXPayEntryActivity.class.getSimpleName(), resp.errCode);
		finish();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
