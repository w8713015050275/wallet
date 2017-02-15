package com.letv.wallet.setting.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.letv.tracker.enums.EventType;
import com.letv.wallet.account.aidl.v1.AccountConstant;
import com.letv.wallet.base.util.Action;
import com.letv.wallet.common.activity.BaseWebViewActivity;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by changjiajie on 16-6-28.
 */
public class SettingWebActivity extends BaseWebViewActivity {

    @Override
    protected boolean needUpdateTitle() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent != null) {
            String pwdStatus = intent.getStringExtra(SettingConstant.EXTRA_PWDSTATUS_KEY);
            if (!TextUtils.isEmpty(pwdStatus)) {
                if (AccountConstant.BASIC_ACCOUNT_PWD_STATE_SETTLED.equals(pwdStatus)) {
                    Action.uploadCustom(EventType.Expose, Action.SETTING_MOD_PWD_PAGE_EXPOSE);
                } else {
                    Action.uploadCustom(Action.EVENTTYPE_SET, Action.SETTING_SET_PWD_PAGE_EXPOSE);
                }
            }
        }
    }
}
