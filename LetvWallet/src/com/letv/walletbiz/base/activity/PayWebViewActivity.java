package com.letv.walletbiz.base.activity;

import android.os.Bundle;
import android.webkit.WebSettings;

import com.letv.wallet.common.activity.BaseWebViewActivity;

/**
 * Created by linquan on 16-2-4.
 */
public class PayWebViewActivity extends BaseWebViewActivity {
    private static final String TAG = "PayWebView";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WebSettings settings = mWebView.getSettings();
        settings.setTextZoom(100);
    }
}

