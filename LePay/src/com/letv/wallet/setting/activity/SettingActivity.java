package com.letv.wallet.setting.activity;

import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.letv.wallet.R;
import com.letv.wallet.account.activity.AccountVerifyActivity;
import com.letv.wallet.account.aidl.v1.AccountConstant;
import com.letv.wallet.account.aidl.v1.AccountInfo;
import com.letv.wallet.account.aidl.v1.RedirectURL;
import com.letv.wallet.account.task.AccountCommonCallback;
import com.letv.wallet.account.task.AccountCreateTask;
import com.letv.wallet.account.task.AccountQueryTask;
import com.letv.wallet.account.task.RedirectTask;
import com.letv.wallet.account.utils.AccountUtils;
import com.letv.wallet.base.util.Action;
import com.letv.wallet.base.util.WalletConstant;
import com.letv.wallet.common.activity.BaseFragmentActivity;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.CommonConstants;
import com.letv.wallet.common.util.ExecutorHelper;
import com.letv.wallet.common.util.LogHelper;
import com.letv.wallet.common.util.NetworkHelper;

import java.io.IOException;

/**
 * Created by changjiajie on 16-5-25.
 */
public class SettingActivity extends BaseFragmentActivity implements View.OnClickListener, AccountHelper.OnAccountChangedListener {

    public static final String TAG = SettingActivity.class.getSimpleName();

    private LinearLayout mAccountVerifyLl;
    private ImageView mAccountVerifyIv;
    private TextView mAccountVerifyTv;
    private TextView mSettingPwdTv;
    private AccountQueryTask mAccountQueryTask;
    private RedirectTask mRedirectTask;
    private RedirectURL mRedirectURL;
    private AccountCreateTask mCreateTask;
    private AccountInfo.BasicAccount mBasicAccount;
    private boolean isFirstShow = true;
    private boolean hasCreateAccount = false;
    private String mFrom;

    private static final int NO_NETWORK_PROMPT = 1;

    private static final int GOPWDPAGE = 1;

    protected Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case NO_NETWORK_PROMPT:
                    Toast.makeText(SettingActivity.this, R.string.pay_no_network, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerNetWorkReceiver();
        AccountHelper.getInstance().registerOnAccountChangeListener(this);
        setContentView(R.layout.lepay_activity_setting);
        initV();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (isNetworkAvailable()) {
            checkAccountStatus();
        }
    }

    @Override
    protected void onNetWorkChanged(boolean isNetworkAvailable) {
        if (isNetworkAvailable && AccountHelper.getInstance().isLogin(getBaseContext())) {
            checkAccountStatus();
        }
    }


    private void clickVerifyLl() {
        // 判断是否开户
        if (checkCreateAccount(false, true)) {
            if (mBasicAccount != null) {
                if (AccountConstant.BASIC_ACCOUNT_VERIFY_STATE_AUTHENTICATED.equals(mBasicAccount.verifyStatus)) {
                    Toast.makeText(getBaseContext(), R.string.prompt_str_account_verified, Toast.LENGTH_SHORT).show();
                } else {
                    goAccountVerifyPage(SettingConstant.VERIFYPAGE_REALNAME_FROM);
                }
            } else {
                checkAccountInfo(true);
            }
        }
    }

    private void clickSettingPwdLl() {
        // 判断是否开户
        if (checkCreateAccount(false, true)) {
            if (mBasicAccount != null) {
                if (!AccountConstant.BASIC_ACCOUNT_VERIFY_STATE_AUTHENTICATED.equals(mBasicAccount.verifyStatus)) {
                    goAccountVerifyPage(SettingConstant.VERIFYPAGE_PWD_FROM);
                    return;
                }
                if (mRedirectURL != null) {
                    goPwdPage(mRedirectURL);
                    return;
                }
                getPwdUrl(GOPWDPAGE, true);
            } else {
                checkAccountInfo(true);
            }
        }
    }

    private void checkStatusClick(final int vId) {
        if (!NetworkHelper.isNetworkAvailable()) {
            if (mHandler != null) {
                Message msg = mHandler.obtainMessage(NO_NETWORK_PROMPT);
                mHandler.sendMessage(msg);
            }
            return;
        }
        if (AccountHelper.getInstance().isLogin(getBaseContext())) {
            switch (vId) {
                case R.id.setting_account_verify_ll:
                    clickVerifyLl();
                    break;
                case R.id.setting_pwd_tv:
                    clickSettingPwdLl();
                    break;
            }
        } else {
            AccountHelper.getInstance().loginLetvAccountIfNot(SettingActivity.this, new AccountManagerCallback() {

                @Override
                public void run(AccountManagerFuture future) {
                    try {
                        if (getBaseContext() != null && future.getResult() != null && AccountHelper.getInstance().isLogin(getBaseContext())) {
                            switch (vId) {
                                case R.id.setting_account_verify_ll:
                                case R.id.setting_pwd_tv:
                                    checkAccountInfo(true);
                                    break;
                            }
                        }
                    } catch (OperationCanceledException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (AuthenticatorException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.setting_account_verify_ll:
            case R.id.setting_pwd_tv:
                checkStatusClick(v.getId());
                break;
        }
    }

    @Override
    public void onAccountLogin() {
    }

    @Override
    public void onAccountLogout() {
        hasCreateAccount = false;
        mCreateTask = null;
        mAccountQueryTask = null;
        mBasicAccount = null;
        mRedirectURL = null;
    }

    @Override
    protected void onDestroy() {
        AccountHelper.getInstance().unregisterOnAccountChangeListener(this);
        super.onDestroy();
    }

    @Override
    public boolean hasBlankAndLoadingView() {
        return true;
    }

    private boolean checkCreateAccount(boolean isForceCreate, boolean isPrompt) {
        hasCreateAccount = AccountUtils.hasCreatedAccount();
        if (!hasCreateAccount) {
            if (isPrompt) {
                Toast.makeText(this, R.string.account_card_unavailable, Toast.LENGTH_SHORT).show();
            }
            if (isForceCreate) {
                showLoadingView();
                createAccount();
            }
        }
        return hasCreateAccount;
    }

    private void createAccount() {
        if (mCreateTask == null) {
            mCreateTask = new AccountCreateTask(new AccountCommonCallback() {
                @Override
                public void onSuccess(Object result) {
                    mCreateTask = null;
                    hasCreateAccount = true;
                    hideLoadingView();
                    //开户成功，请求账户信息
                    checkAccountInfo(true);
                }

                @Override
                public void onError(int errorCode, String errorMsg) {
                    mCreateTask = null;
                    accountInfoError();
                    hideLoadingView();
                    LogHelper.e("[%S] createAccount | errorCode == %s | errorMsg == %s", TAG, errorCode + "", errorMsg);
                    switch (errorCode) {
                        case AccountConstant.RspCode.ERRNO_USER:
                            if (!TextUtils.isEmpty(errorMsg)) {
                                Toast.makeText(SettingActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                            }
                            break;
                    }
                }

                @Override
                public void onNoNet() {
                    mCreateTask = null;
                    accountInfoError();
                    hideLoadingView();
                    if (mHandler != null) {
                        Message msg = mHandler.obtainMessage(NO_NETWORK_PROMPT);
                        mHandler.sendMessage(msg);
                    }
                }
            });
            ExecutorHelper.getExecutor().runnableExecutor(mCreateTask);
        }
    }

    private class AccountQueryCallback implements AccountCommonCallback<AccountInfo> {

        private boolean mIsPrompt;

        public AccountQueryCallback(boolean isPrompt) {
            this.mIsPrompt = isPrompt;
        }

        @Override
        public void onSuccess(AccountInfo result) {
            hideLoadingView();
            mAccountQueryTask = null;
            updateInfo(result, this.mIsPrompt);
        }

        @Override
        public void onError(int errorCode, String errorMsg) {
            mAccountQueryTask = null;
            if (mBasicAccount == null) {
                accountInfoError();
            }
            hideLoadingView();
            LogHelper.e("[%S] account_query | errorCode == %s | errorMsg == %s", TAG, errorCode + "", errorMsg);
            switch (errorCode) {
                case AccountConstant.RspCode.ERRNO_USER:
                    if (this.mIsPrompt && !TextUtils.isEmpty(errorMsg)) {
                        Toast.makeText(SettingActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }

        @Override
        public void onNoNet() {
            mAccountQueryTask = null;
            if (mBasicAccount == null) {
                accountInfoError();
            }
            hideLoadingView();
            if (this.mIsPrompt && mHandler != null) {
                Message msg = mHandler.obtainMessage(NO_NETWORK_PROMPT);
                mHandler.sendMessage(msg);
            }
        }
    }

    private class PwdUrlCallback implements AccountCommonCallback<RedirectURL> {

        private int mType;
        private boolean mIsPrompt;

        public PwdUrlCallback(int type, boolean isPrompt) {
            this.mType = type;
            this.mIsPrompt = isPrompt;
        }

        @Override
        public void onSuccess(RedirectURL result) {
            mRedirectTask = null;
            hideLoadingView();
            if (result == null) {
                LogHelper.e("[%S] pwd_url_finish | result == null", TAG);
                if (this.mIsPrompt && mHandler != null) {
                    Message msg = mHandler.obtainMessage(NO_NETWORK_PROMPT);
                    mHandler.sendMessage(msg);
                }
                return;
            }
            mRedirectURL = result;
            if (mType == GOPWDPAGE) {
                goPwdPage(mRedirectURL);
            }
        }

        @Override
        public void onError(int errorCode, String errorMsg) {
            mRedirectTask = null;
            hideLoadingView();
            LogHelper.e("[%S] pwd_url_finish | errorCode == %s | errorMsg == %s", TAG, errorCode + "", errorMsg);
            switch (errorCode) {
                case AccountConstant.RspCode.ERROR_NETWORK:
                    if (this.mIsPrompt && mHandler != null) {
                        Message msg = mHandler.obtainMessage(NO_NETWORK_PROMPT);
                        mHandler.sendMessage(msg);
                    }
                    break;
                case AccountConstant.RspCode.ERRNO_USER:
                    if (this.mIsPrompt && !TextUtils.isEmpty(errorMsg)) {
                        Toast.makeText(SettingActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }

        @Override
        public void onNoNet() {
            mRedirectTask = null;
            hideLoadingView();
            if (this.mIsPrompt && mHandler != null) {
                Message msg = mHandler.obtainMessage(NO_NETWORK_PROMPT);
                mHandler.sendMessage(msg);
            }
        }

    }

    private void accountInfoError() {
        mBasicAccount = null;
        mAccountVerifyTv.setText(R.string.setting_account_not_verify);
        mAccountVerifyIv.setSelected(false);
    }

    private void updateInfo(AccountInfo accountInfo, boolean isPrompt) {
        if (accountInfo == null) {
            LogHelper.e("[%S] account_query_finish | accountInfo == null", TAG);
            if (isPrompt && mHandler != null) {
                Message msg = mHandler.obtainMessage(NO_NETWORK_PROMPT);
                mHandler.sendMessage(msg);
            }
            return;
        }
        if (accountInfo.basic == null) {
            LogHelper.e("[%S] account_query_finish | AccountInfo.basic == null", TAG);
            if (isPrompt && mHandler != null) {
                Message msg = mHandler.obtainMessage(NO_NETWORK_PROMPT);
                mHandler.sendMessage(msg);
            }
            return;
        }
        if (mRedirectURL == null) {
            getPwdUrl(0, false);
        }
        if (isFirstShow) {
            isFirstShow = false;
            Action.uploadExpose(Action.TAB_SET_PAGE_EXPOSE, mFrom);
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
        intent.putExtra(SettingConstant.EXTRA_PWDSTATUS_KEY, mBasicAccount.pwdStatus);
        startActivity(intent);
    }

    private void goAccountVerifyPage(String from) {
        Intent intent = new Intent(SettingActivity.this, AccountVerifyActivity.class);
        intent.putExtra(CommonConstants.EXTRA_FROM, from);
        startActivity(intent);
    }

    private void initV() {
        processExtraData();
        findViewById();
        mAccountVerifyLl.setOnClickListener(this);
        mSettingPwdTv.setOnClickListener(this);
    }

    private void processExtraData() {
        Intent intent = getIntent();
        if (intent != null) {
            mFrom = intent.getStringExtra(WalletConstant.EXTRA_FROM);
        }
    }

    private void getPwdUrl(int type, boolean isPrompt) {
        String[] jtype = new String[2];
        jtype[0] = AccountConstant.JTYPE_MOD_PAY_PWD;
        jtype[1] = AccountConstant.JTYPE_SET_PAY_PWD;
        showLoadingView();
        mRedirectTask = new RedirectTask(jtype, new PwdUrlCallback(type, isPrompt));
        ExecutorHelper.getExecutor().runnableExecutor(mRedirectTask);
    }

    private void checkAccountStatus() {
        if (checkCreateAccount(true, false)) {
            checkAccountInfo(false);
        }
    }

    private void checkAccountInfo(boolean isPrompt) {
        if (!isNetworkAvailable()) {
            if (mHandler != null) {
                Message msg = mHandler.obtainMessage(NO_NETWORK_PROMPT);
                mHandler.sendMessage(msg);
            }
            return;
        }
        if (mBasicAccount != null) {
            if (mRedirectURL == null) {
                getPwdUrl(0, false);
            } else {
                hideBlankPage();
            }
            if (!AccountUtils.hasVerifyAccount()) {
                showLoadingView();
                if (mAccountQueryTask == null) {
                    mAccountQueryTask = new AccountQueryTask(AccountConstant.QTYPE_BASIC,
                            new AccountQueryCallback(isPrompt));
                }
                ExecutorHelper.getExecutor().runnableExecutor(mAccountQueryTask);
            }
        } else {
            showLoadingView();
            if (mAccountQueryTask == null) {
                mAccountQueryTask = new AccountQueryTask(AccountConstant.QTYPE_BASIC,
                        new AccountQueryCallback(isPrompt));
            }
            ExecutorHelper.getExecutor().runnableExecutor(mAccountQueryTask);
        }
    }

    private void findViewById() {
        mAccountVerifyLl = (LinearLayout) findViewById(R.id.setting_account_verify_ll);
        mAccountVerifyTv = (TextView) findViewById(R.id.setting_account_verify_tv);
        mAccountVerifyIv = (ImageView) findViewById(R.id.setting_account_verify_iv);
        mSettingPwdTv = (TextView) findViewById(R.id.setting_pwd_tv);
    }

}
