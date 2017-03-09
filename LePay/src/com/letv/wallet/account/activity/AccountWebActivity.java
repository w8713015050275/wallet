package com.letv.wallet.account.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.letv.wallet.account.aidl.v1.AccountConstant;
import com.letv.wallet.account.aidl.v1.RedirectURL;
import com.letv.wallet.account.task.AccountCommonCallback;
import com.letv.wallet.account.task.RedirectTask;
import com.letv.wallet.base.util.Action;
import com.letv.wallet.common.activity.BaseWebViewActivity;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.ExecutorHelper;
import com.letv.wallet.common.util.NetworkHelper;
import com.letv.wallet.common.view.BlankPage;

/**
 * Created by lijunying on 17-2-10.
 */

public class AccountWebActivity extends BaseWebViewActivity implements AccountHelper.OnAccountChangedListener {
    public static final String EXTRA_KEY_JTYPE = "Jtype";
    private RedirectTask task;
    private String jType = null;

    @Override
    protected boolean needUpdateTitle() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AccountHelper.getInstance().registerOnAccountChangeListener(this);
        Intent intent = getIntent();
        if (intent != null) { //url为空， （未登录或者type为空） finish
            if (TextUtils.isEmpty(mUrl) && (TextUtils.isEmpty(jType = intent.getStringExtra(EXTRA_KEY_JTYPE)) || !AccountHelper.getInstance().isLogin(this))) {
                finish();
                return;
            }
            if (AccountConstant.JTYPE_ADD_CARD.equalsIgnoreCase(jType)) {
                Action.uploadExpose(Action.ACCOUNT_CARD_BIND_EXPOSE);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (TextUtils.isEmpty(mUrl)  && checkNetWork()) {
            redirect(jType);
        }
    }

    @Override
    public void setFlagIfNeeded(Intent intent) {
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }

    @Override
    protected void onNetWorkChanged(boolean isNetworkAvailable) {
        if (isNetworkAvailable && TextUtils.isEmpty(mUrl)) {
            redirect(jType);
        }
    }

    @Override
    public boolean hasRedirect() {
        return true;
    }

    @Override
    public void onAccountLogin() {

    }

    @Override
    public void onAccountLogout() {
        if (TextUtils.isEmpty(mUrl)) {
            finish(); //换账户后，导致type和当前账户状态不统一 ；
        }
    }

    private boolean checkNetWork(){
        if (NetworkHelper.isNetworkAvailable()) {
            return true;
        }
        showBlankPage(BlankPage.STATE_NO_NETWORK);
        return false;
    }

    private void redirect(final String jType){
        if (task == null) {
            task = new RedirectTask(new String[]{jType}, new AccountCommonCallback<RedirectURL>() {
                @Override
                public void onSuccess(RedirectURL result) {
                    task = null;
                    if (result != null && !TextUtils.isEmpty(mUrl = result.getUrl(jType))) {
                        loadPage();
                    } else {
                        showNetErrorBlankPage();
                    }
                }

                @Override
                public void onError(int errorCode, String errorMsg) {
                    hideLoadingView();
                    task = null;
                    showNetErrorBlankPage();
                }

                @Override
                public void onNoNet() {
                    hideLoadingView();
                    task = null;
                    showBlankPage(BlankPage.STATE_NO_NETWORK);
                }
            });
            showLoadingView();
            ExecutorHelper.getExecutor().runnableExecutor(task);
        }
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
}
