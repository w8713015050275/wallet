package com.letv.wallet.account.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.letv.wallet.R;
import com.letv.wallet.account.aidl.v1.RedirectURL;
import com.letv.wallet.account.task.AccountCommonCallback;
import com.letv.wallet.account.task.RedirectTask;
import com.letv.wallet.common.activity.BaseFragmentActivity;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.CommonConstants;
import com.letv.wallet.common.util.ExecutorHelper;
import com.letv.wallet.common.util.NetworkHelper;
import com.letv.wallet.common.view.BlankPage;

/**
 * Created by lijunying on 17-2-15.
 */

public class RedirectActivity extends BaseFragmentActivity{

    public static final String EXTRA_KEY_JTYPE = "redirectJtype";
    private RedirectTask task;
    private FrameLayout mContainer;
    private String jType = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme_WebView);
        Intent intent = getIntent();
        if (intent != null) {
            jType = intent.getStringExtra(EXTRA_KEY_JTYPE);
        }
        if (TextUtils.isEmpty(jType)) {
            finish();
        }

        registerNetWorkReceiver();
        mContainer = new FrameLayout(this);
        mContainer.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(mContainer);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (checkLogin() && checkNetWork()) {
            redirect(jType);
        }
    }

    private boolean checkLogin() {
        if(AccountHelper.getInstance().isLogin(this)){
            return true;
        }
        showBlankPage(BlankPage.STATE_NO_LOGIN);
        return false ;
    }

    private boolean checkNetWork(){
        if (NetworkHelper.isNetworkAvailable()) {
            return true;
        }
        showBlankPage(BlankPage.STATE_NO_NETWORK);
        return false;
    }


    @Override
    public boolean hasBlankAndLoadingView() {
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void redirect(final String jType){
        if (task == null) {
            task = new RedirectTask(new String[]{jType}, new AccountCommonCallback<RedirectURL>() {
                @Override
                public void onSuccess(RedirectURL result) {
                    hideLoadingView();
                    task = null;
                    if (result != null && !TextUtils.isEmpty(result.getUrl(jType))) {
                        Intent intent = new Intent(RedirectActivity.this, AccountWebActivity.class);
                        intent.putExtra(CommonConstants.EXTRA_URL, result.getUrl(jType));
                        intent.putExtra(AccountWebActivity.EXTRA_KEY_JTYPE, jType);
                        startActivity(intent);
                        finish();
                    }
                    showBlankPage(BlankPage.STATE_NETWORK_ABNORMAL);
                }

                @Override
                public void onError(int errorCode, String errorMsg) {
                    hideLoadingView();
                    task = null;
                    showBlankPage(BlankPage.STATE_NETWORK_ABNORMAL);
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

    @Override
    protected void onNetWorkChanged(boolean isNetworkAvailable) {
        if (isNetworkAvailable) {
            redirect(jType);
        }
    }
}
