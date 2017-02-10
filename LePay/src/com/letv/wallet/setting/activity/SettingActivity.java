package com.letv.wallet.setting.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.letv.wallet.R;
import com.letv.wallet.account.activity.AccountVerifyActivity;
import com.letv.wallet.account.aidl.v1.AccountConstant;
import com.letv.wallet.account.aidl.v1.AccountInfo;
import com.letv.wallet.account.aidl.v1.RedirectURL;
import com.letv.wallet.account.task.AccountCommonCallback;
import com.letv.wallet.account.task.AccountQueryTask;
import com.letv.wallet.account.task.RedirectTask;
import com.letv.wallet.common.activity.BaseFragmentActivity;
import com.letv.wallet.common.util.CommonConstants;
import com.letv.wallet.common.util.ExecutorHelper;
import com.letv.wallet.common.util.LogHelper;
import com.letv.wallet.common.util.NetworkHelper;
import com.letv.wallet.common.view.BlankPage;

/**
 * Created by changjiajie on 16-5-25.
 */
public class SettingActivity extends BaseFragmentActivity implements View.OnClickListener {

    public static final String TAG = SettingActivity.class.getSimpleName();

    private RelativeLayout mAccountVerifyLl;
    private ImageView mAccountVerifyIv;
    private TextView mAccountVerifyTv;
    private TextView mSettingPwdTv;
    private AccountQueryTask mAccountQueryTask;
    private RedirectTask mRedirectTask;
    private RedirectURL mRedirectURL;
    private AccountInfo.BasicAccount mBasicAccount;

    private static final int ACCOUNT_QUERY_FINISH = 1;
    private static final int PWD_URL_FINISH = 2;
    private static final int NO_NETWORK_PROMPT = 3;
    private static final int NO_NETWORK_SHOW_BLANKPAGE = 4;

    protected Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case ACCOUNT_QUERY_FINISH:
                    mAccountQueryTask = null;
                    hideLoadingView();
                    if (msg.obj == null) {
                        LogHelper.e("[%S] account_query_finish | msg.obj == null", TAG);
                        showBlankPage(BlankPage.STATE_DATA_EXCEPTION, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                checkAccountInfo();
                            }
                        });
                        return;
                    }
                    updateInfo((AccountInfo) msg.obj);
                    break;
                case PWD_URL_FINISH:
                    mRedirectTask = null;
                    hideLoadingView();
                    if (msg.obj == null) {
                        LogHelper.e("[%S] pwd_url_finish | msg.obj == null", TAG);
                        Toast.makeText(SettingActivity.this, R.string.pay_no_network, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    mRedirectURL = (RedirectURL) msg.obj;
                    goPwdPage(mRedirectURL);
                    break;
                case NO_NETWORK_PROMPT:
                    Toast.makeText(SettingActivity.this, R.string.pay_no_network, Toast.LENGTH_SHORT).show();
                    break;
                case NO_NETWORK_SHOW_BLANKPAGE:
                    showBlankPage(BlankPage.STATE_NO_NETWORK, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            checkAccountInfo();
                        }
                    });
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lepay_activity_setting);
        initV();
    }

    @Override
    public boolean hasBlankAndLoadingView() {
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        checkAccountInfo();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.setting_account_verify_ll:
                if (!NetworkHelper.isNetworkAvailable()) {
                    Message msg = mHandler.obtainMessage(NO_NETWORK_PROMPT);
                    mHandler.sendMessage(msg);
                    return;
                }
                if (mBasicAccount != null) {
                    if (AccountConstant.BASIC_ACCOUNT_VERIFY_STATE_AUTHENTICATED.equals(mBasicAccount.verifyStatus)) {
                        Toast.makeText(getBaseContext(), R.string.prompt_str_account_verified, Toast.LENGTH_SHORT).show();
                    } else {
                        goAccountVerifyPage();
                    }
                }
                break;
            case R.id.setting_pwd_tv:
                if (!NetworkHelper.isNetworkAvailable()) {
                    Message msg = mHandler.obtainMessage(NO_NETWORK_PROMPT);
                    mHandler.sendMessage(msg);
                    return;
                }
                if (mBasicAccount != null) {
                    if (!AccountConstant.BASIC_ACCOUNT_VERIFY_STATE_AUTHENTICATED.equals(mBasicAccount.verifyStatus)) {
                        goAccountVerifyPage();
                        return;
                    }
                    if (mRedirectURL != null) {
                        goPwdPage(mRedirectURL);
                        return;
                    }
                    getPwdUrl();
                }
                break;
        }
    }

    private class AccountQueryCallback implements AccountCommonCallback<AccountInfo> {

        @Override
        public void onSuccess(AccountInfo result) {
            if (mHandler != null) {
                Message msg = mHandler.obtainMessage(ACCOUNT_QUERY_FINISH);
                msg.obj = result;
                mHandler.sendMessage(msg);
            }
        }

        @Override
        public void onError(int errorCode, String errorMsg) {
            if (mHandler != null) {
                Message msg = mHandler.obtainMessage(ACCOUNT_QUERY_FINISH);
                msg.arg1 = errorCode;
                mHandler.sendMessage(msg);
            }
        }

        @Override
        public void onNoNet() {
            if (mBasicAccount == null) {
                if (mHandler != null) {
                    Message msg = mHandler.obtainMessage(NO_NETWORK_SHOW_BLANKPAGE);
                    mHandler.sendMessage(msg);
                }
            }
        }
    }

    private class PwdUrlCallback implements AccountCommonCallback<RedirectURL> {

        @Override
        public void onSuccess(RedirectURL result) {
            if (mHandler != null) {
                Message msg = mHandler.obtainMessage(PWD_URL_FINISH);
                msg.obj = result;
                mHandler.sendMessage(msg);
            }
        }

        @Override
        public void onError(int errorCode, String errorMsg) {
            if (mHandler != null) {
                Message msg = mHandler.obtainMessage(PWD_URL_FINISH);
                msg.arg1 = errorCode;
                mHandler.sendMessage(msg);
            }
        }

        @Override
        public void onNoNet() {
            if (mHandler != null) {
                Message msg = mHandler.obtainMessage(NO_NETWORK_PROMPT);
                mHandler.sendMessage(msg);
            }
        }
    }

    private void updateInfo(AccountInfo accountInfo) {
        if (accountInfo.basic == null) {
            LogHelper.e("[%S] account_query_finish | AccountInfo.basic == null", TAG);
            showBlankPage(BlankPage.STATE_DATA_EXCEPTION, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkAccountInfo();
                }
            });
            return;
        }
        mBasicAccount = accountInfo.basic;
        if (AccountConstant.BASIC_ACCOUNT_VERIFY_STATE_AUTHENTICATED.equals(mBasicAccount.verifyStatus)) {
            mAccountVerifyTv.setText(R.string.setting_account_verified);
            mAccountVerifyIv.setSelected(true);
        } else {
            mAccountVerifyTv.setText(R.string.setting_account_not_verify);
            mAccountVerifyIv.setSelected(false);
        }
    }

    private void goPwdPage(RedirectURL url) {
        String urlStr;
        String titleStr;
        if (AccountConstant.BASIC_ACCOUNT_PWD_STATE_SETTLED.equals(mBasicAccount.pwdStatus)) {
            urlStr = url.mod_pay_pwd;
            titleStr = getString(R.string.pay_pwd_mod_title);
        } else {
            urlStr = url.set_pay_pwd;
            titleStr = getString(R.string.pay_pwd_set_title);
        }
        if (TextUtils.isEmpty(urlStr)) {
            LogHelper.e("[%S] url == null", TAG);
            return;
        }
        Intent intent = new Intent(SettingActivity.this, SettingWebActivity.class);
        intent.putExtra(CommonConstants.EXTRA_URL, urlStr);
        intent.putExtra(CommonConstants.EXTRA_TITLE_NAME, titleStr);
        startActivity(intent);
    }

    private void goAccountVerifyPage() {
        Intent intent = new Intent(SettingActivity.this, AccountVerifyActivity.class);
        startActivity(intent);
    }

    private void initV() {
        findViewById();
        mAccountVerifyLl.setOnClickListener(this);
        mSettingPwdTv.setOnClickListener(this);
        checkAccountInfo();
    }

    private void getPwdUrl() {
        String[] jtype = new String[2];
        jtype[0] = AccountConstant.JTYPE_MOD_PAY_PWD;
        jtype[1] = AccountConstant.JTYPE_SET_PAY_PWD;
        mRedirectTask = new RedirectTask(jtype, new PwdUrlCallback());
        showLoadingView();
        ExecutorHelper.getExecutor().runnableExecutor(mRedirectTask);
    }

    private void checkAccountInfo() {
        if (mAccountQueryTask == null) {
            mAccountQueryTask = new AccountQueryTask(AccountConstant.QTYPE_BASIC,
                    new AccountQueryCallback());
        }
        showLoadingView();
        ExecutorHelper.getExecutor().runnableExecutor(mAccountQueryTask);
    }

    private void findViewById() {
        mAccountVerifyLl = (RelativeLayout) findViewById(R.id.setting_account_verify_ll);
        mAccountVerifyTv = (TextView) findViewById(R.id.setting_account_verify_tv);
        mAccountVerifyIv = (ImageView) findViewById(R.id.setting_account_verify_iv);
        mSettingPwdTv = (TextView) findViewById(R.id.setting_pwd_tv);
    }

}