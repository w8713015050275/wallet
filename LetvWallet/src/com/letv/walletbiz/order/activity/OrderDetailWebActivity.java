package com.letv.walletbiz.order.activity;

import android.os.Bundle;

import com.letv.wallet.common.activity.BaseWebViewActivity;

/**
 * Created by zhanghuancheng on 17-3-13.
 */

public class OrderDetailWebActivity extends BaseWebViewActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected boolean needUpdateTitle() {
        return true;
    }

}
