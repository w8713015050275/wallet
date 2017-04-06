package com.letv.walletbiz.me.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.letv.wallet.account.LePayAccountManager;
import com.letv.wallet.account.LePayCommonCallback;
import com.letv.wallet.account.aidl.v1.AccountConstant;
import com.letv.wallet.account.aidl.v1.RedirectURL;
import com.letv.wallet.common.BaseApplication;
import com.letv.wallet.common.activity.BaseWebViewActivity;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.LogHelper;
import com.letv.wallet.common.util.NetworkHelper;
import com.letv.wallet.common.view.BlankPage;
import com.letv.walletbiz.R;

/**
 * Created by lijunying on 17-3-3.
 */

public class AccountWebActivity extends BaseWebViewActivity implements AccountHelper.OnAccountChangedListener {
    public static final String EXTRA_KEY_JTYPE = "redirectJtype";

    private String jType = null;

    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AccountHelper.getInstance().registerOnAccountChangeListener(this);
        Intent intent = getIntent();
        if (intent != null) { //url为空， （未登录或者type为空） finish
            if (TextUtils.isEmpty(mUrl) && (TextUtils.isEmpty(jType = intent.getStringExtra(EXTRA_KEY_JTYPE)) || !AccountHelper.getInstance().isLogin(this))) {
               finish();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (TextUtils.isEmpty(mUrl) && checkNetWork()) {
            redirect(jType);
        }
    }

    @Override
    protected void onDestroy() {
        AccountHelper.getInstance().unregisterOnAccountChangeListener(this);
        super.onDestroy();
    }

    @Override
    protected boolean needUpdateTitle() {
        return true;
    }

    @Override
    public boolean hasRedirect() {
        return true;
    }

    @Override
    protected void onNetWorkChanged(boolean isNetworkAvailable) {
        if (isNetworkAvailable ) {
            if (TextUtils.isEmpty(mUrl)) {
                redirect(jType);
            } else {
                super.onNetWorkChanged(isNetworkAvailable);
            }

        }
    }

    @Override
    public void onAccountLogin() {

    }

    @Override
    public void onAccountLogout() {
        finish(); //换账户后，返回上一页；
    }

    private boolean checkNetWork(){
        if (NetworkHelper.isNetworkAvailable()) {
            return true;
        }
        showBlankPage(BlankPage.STATE_NO_NETWORK);
        return false;
    }

    private void showNetErrorBlankPage(){
        showBlankPage(BlankPage.STATE_NETWORK_ABNORMAL, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideBlankPage();
                redirect(jType);
            }
        });
    }

    private void redirect(final String jType){
        if (isLoading) { return; }

        isLoading = true;
        showLoadingView();

        LePayAccountManager.getInstance().redirect(new String[]{jType}, new LePayCommonCallback<RedirectURL>() {
            @Override
            public void onSuccess(RedirectURL result) {
                isLoading = false;
                if (result != null && !TextUtils.isEmpty(mUrl = result.getUrl(jType))) {
                    loadPage();
                } else {
                    showNetErrorBlankPage();
                }
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                isLoading = false;
                hideLoadingView();
                if (errorCode == AccountConstant.RspCode.ERRNO_NO_NETWORK) {
                    showBlankPage(BlankPage.STATE_NO_NETWORK);
                }else if(errorCode == AccountConstant.RspCode.ERRNO_USER_AUTH_FAILED){
                    Toast.makeText(BaseApplication.getApplication(), R.string.account_login_expired, Toast.LENGTH_SHORT).show();
                    finish(); //token 过期，finish返回上界面
                }else {
                    showNetErrorBlankPage();
                    LogHelper.e("redirect jType = "+ jType + " onError = " + errorCode);
                }
            }
        });
    }

}
