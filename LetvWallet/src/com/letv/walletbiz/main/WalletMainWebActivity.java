package com.letv.walletbiz.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;

import com.google.gson.JsonObject;
import com.letv.wallet.common.activity.BaseWebViewActivity;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.walletbiz.base.util.WalletConstant;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by liuliang on 16-4-14.
 */
public class WalletMainWebActivity extends BaseWebViewActivity {

    private static final String JS_GETSSOTK_INTERFACE_NAME = "LeWalletJSBridge";
    private static final String JSON_ERRNO = "errno";
    private static final String JSON_ERRMSG = "errmsg";
    private static final String JSON_DATA = "data";
    private static final String JSON_SUCCESS_MSG = "success";
    private static final String JSON_NOTOKEN_MSG = "Get token failed";
    private static final String JSON_NOLOGIN_MSG = "Redirecting login...";

    private static final int SUCCESS = 10000;
    private static final int NOTOKEN = 10001;
    private static final int NOLOGIN = 10002;

    private boolean withAccount = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        if (intent != null) {
            withAccount = intent.getBooleanExtra(WalletConstant.EXTRA_WEB_WITH_ACCOUNT, false);
        }
        super.onCreate(savedInstanceState);
        mWebView.addJavascriptInterface(new JsBridgeInterface(this), JS_GETSSOTK_INTERFACE_NAME);
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
        public String GetSsoTk() {
            if (!AccountHelper.getInstance().isLogin(this.mContext)) {
                AccountHelper.getInstance().loginLetvAccountIfNot(this.mContext, null);
                return getJson(NOLOGIN, JSON_NOLOGIN_MSG, "");
            } else {
                String uToken = AccountHelper.getInstance().getToken(this.mContext);
                if (TextUtils.isEmpty(uToken))
                    return getJson(NOTOKEN, JSON_NOTOKEN_MSG, "");
                return getJson(SUCCESS, JSON_SUCCESS_MSG, uToken);
            }
        }

    }

    public String getJson(int errno, String errmsg, String data) {
        JsonObject obj = new JsonObject();
        obj.addProperty(JSON_ERRNO, errno);
        obj.addProperty(JSON_ERRMSG, errmsg);
        obj.addProperty(JSON_DATA, data);
        return obj.toString();
    }
}
