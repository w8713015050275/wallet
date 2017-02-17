package com.letv.wallet.account.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.letv.wallet.account.aidl.v1.AccountConstant;
import com.letv.wallet.account.utils.ActionUtils;
import com.letv.wallet.base.util.Action;
import com.letv.wallet.common.activity.BaseWebViewActivity;

/**
 * Created by lijunying on 17-2-10.
 */

public class AccountWebActivity extends BaseWebViewActivity {
    public static final String EXTRA_KEY_JTYPE = "Jtype";

    @Override
    protected boolean needUpdateTitle() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent != null) {
            String jType = getIntent().getStringExtra(EXTRA_KEY_JTYPE);
            if (AccountConstant.JTYPE_ADD_CARD.equalsIgnoreCase(jType)) {
                Action.uploadExpose(Action.ACCOUNT_CARD_BIND_EXPOSE, ActionUtils.getFromExtra(intent));
            }
        }
    }
}
