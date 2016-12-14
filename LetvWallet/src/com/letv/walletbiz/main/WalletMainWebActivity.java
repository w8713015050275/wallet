package com.letv.walletbiz.main;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;

import com.letv.wallet.common.activity.BaseWebViewActivity;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.DeviceUtils;
import com.letv.walletbiz.WalletApplication;
import com.letv.walletbiz.base.util.WalletConstant;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by liuliang on 16-4-14.
 */
public class WalletMainWebActivity extends BaseWebViewActivity {

    private static final String JS_Bridge_INTERFACE_NAME = "LeWalletJSBridge";

    private boolean withAccount = false;
    private static final String DOMAIN_LE = "le.com";
    private static final String DOMAIN_LETV = "letv.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        if (intent != null) {
            withAccount = intent.getBooleanExtra(WalletConstant.EXTRA_WEB_WITH_ACCOUNT, false);
        }
        super.onCreate(savedInstanceState);
        Uri uri = Uri.parse(mUrl);
        String host = uri.getHost();
        if (!TextUtils.isEmpty(host) && (host.endsWith(DOMAIN_LE) || host.endsWith(DOMAIN_LETV))) {
            mWebView.addJavascriptInterface(new JsBridgeInterface(this), JS_Bridge_INTERFACE_NAME);
        }
    }

    @Override
    protected boolean needUpdateTitle() {
        return true;
    }

    @Override
    protected Map<String, String> getAdditionalHttpHeaders() {
        HashMap<String,String> headers = null;
        if (withAccount) {
            headers = new HashMap<String,String>();
            headers.put("SSOTK", AccountHelper.getInstance().getToken(this));
        }
        return headers;
    }

    public class JsBridgeInterface {
        private Activity mContext;

        JsBridgeInterface(Activity context) {
            this.mContext = context;
        }

        @JavascriptInterface
        public String getImei() {
            return DeviceUtils.getDeviceImei(WalletApplication.getApplication());
        }

        @JavascriptInterface
        public String getPhoneNumbers() {
            ArrayList<String> numberList = new ArrayList<String>();
            String phoneNumber0 = DeviceUtils.getPhoneNumber0(WalletMainWebActivity.this);
            String phoneNumber1 = DeviceUtils.getPhoneNumber1(WalletMainWebActivity.this);
            if (!TextUtils.isEmpty(phoneNumber0)) {
                numberList.add(phoneNumber0);
            }
            if (!TextUtils.isEmpty(phoneNumber1)) {
                numberList.add(phoneNumber1);
            }
            if (numberList.size() == 0) {
                return null;
            } else {
                JSONArray array = new JSONArray();
                for (String phoneNumber : numberList) {
                    array.put(phoneNumber);
                }
                return array.toString();
            }
        }

    }
}
