package com.letv.wallet.common.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.LayoutRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.letv.shared.widget.LeLoadingView;
import com.letv.wallet.common.R;
import com.letv.wallet.common.util.NetworkHelper;
import com.letv.wallet.common.view.BlankPage;

/**
 * Created by liuliang on 16-3-25.
 */
public abstract class BaseFragmentActivity extends AppCompatActivity {

    private LinearLayout mRootView;
    private Toolbar mToolbar;
    private FrameLayout mLayoutContainer;
    private BlankPage mBlankPage;
    private LeLoadingView mLoadingView;

    private View mCustomView;

    private boolean isRegisteredNetWork = false;
    private boolean isNetworkAvailable = false;
    private static final int MSG_NETWORK_CHANGED = 1;
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_NETWORK_CHANGED:
                    onNetWorkChanged(isNetworkAvailable);
                    break;
            }
        }
    };

    private BroadcastReceiver mNetWokReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                boolean isConnected = NetworkHelper.isNetworkAvailable();
                if (isNetworkAvailable != isConnected) {
                    isNetworkAvailable = isConnected;
                    mHandler.sendEmptyMessage(MSG_NETWORK_CHANGED);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected boolean registerNetWorkReceiver() {
        isNetworkAvailable = NetworkHelper.isNetworkAvailable();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mNetWokReceiver, filter);
        isRegisteredNetWork = true;
        return true;
    }

    @Override
    protected void onDestroy() {
        try {
            if (isRegisteredNetWork && mNetWokReceiver != null) {
                unregisterReceiver(mNetWokReceiver);
            }
        } catch (Exception e) {
        }
        isRegisteredNetWork = false;
        super.onDestroy();
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        if (!hasToolbar() && !hasBlankAndLoadingView()) {
            super.setContentView(layoutResID);
        } else if (!hasToolbar() && hasBlankAndLoadingView()) {
            super.setContentView(R.layout.base_blank_loading_layout);
            initBlankAndLoadingView();
            mCustomView = LayoutInflater.from(this).inflate(layoutResID, mLayoutContainer, false);
            addCustomView(mLayoutContainer, mCustomView, null);
        } else {
            super.setContentView(R.layout.base_activity_toolbar_layout);
            mRootView = (LinearLayout) findViewById(R.id.base_root);
            mToolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(mToolbar);

            mCustomView = LayoutInflater.from(this).inflate(layoutResID, mLayoutContainer, false);

            if (hasBlankAndLoadingView()) {
                LayoutInflater.from(this).inflate(R.layout.base_blank_loading_layout, mRootView, true);
                initBlankAndLoadingView();
                addCustomView(mLayoutContainer, mCustomView, null);
            } else {
                addCustomView(mRootView, mCustomView, null);
            }
        }
    }

    @Override
    public void setContentView(View view) {
        setContentView(view, null);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        if (!hasToolbar() && !hasBlankAndLoadingView()) {
            super.setContentView(view, params);
        } else if (!hasToolbar() && hasBlankAndLoadingView()) {
            super.setContentView(R.layout.base_blank_loading_layout);
            initBlankAndLoadingView();
            mCustomView = view;
            addCustomView(mLayoutContainer, mCustomView, params);
        } else {
            super.setContentView(R.layout.base_activity_toolbar_layout);
            mRootView = (LinearLayout) findViewById(R.id.base_root);
            mToolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(mToolbar);

            mCustomView = view;

            if (hasBlankAndLoadingView()) {
                LayoutInflater.from(this).inflate(R.layout.base_blank_loading_layout, mRootView, true);
                initBlankAndLoadingView();
                addCustomView(mLayoutContainer, mCustomView, params);
            } else {
                addCustomView(mRootView, mCustomView, params);
            }
        }
    }

    public boolean isNetworkAvailable() {
        if (!isRegisteredNetWork) {
            isNetworkAvailable = NetworkHelper.isNetworkAvailable();
        }
        return isNetworkAvailable;
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }

    /**
     * 是否增加错误和loading页面
     * @return
     */
    public abstract boolean hasBlankAndLoadingView();

    /**
     * 是否加载actionbar
     * @return false:不加载actionbar
     */
    public boolean hasToolbar() {
        return true;
    }

    /**
     * 工作在UI线程,必须首先调用 registerNetWorkReceiver()
     */
    protected void onNetWorkChanged(boolean isNetworkAvailable) {
    }

    private void initBlankAndLoadingView() {
        mLayoutContainer = (FrameLayout) findViewById(R.id.base_container);
        mBlankPage = (BlankPage) findViewById(R.id.base_blank_page);
        mLoadingView = (LeLoadingView) findViewById(R.id.base_loading);
    }

    private void addCustomView(ViewGroup parent, View customView, ViewGroup.LayoutParams params) {
        if (parent == null || customView == null) {
            return;
        }
        if (params == null) {
            params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
        parent.addView(customView, params);
    }

    public boolean isBlankPageVisible() {
        return mBlankPage != null && mBlankPage.getVisibility() == View.VISIBLE;
    }

    public BlankPage showBlankPage() {
        if (mBlankPage != null) {
            if (mLoadingView != null) {
                mLoadingView.setVisibility(View.GONE);
            }
            if (mCustomView != null) {
                mCustomView.setVisibility(View.INVISIBLE);
            }
            mBlankPage.setVisibility(View.VISIBLE);
        }
        return mBlankPage;
    }

    public BlankPage showBlankPage(int state) {
        return showBlankPage(state, null);
    }

    /**
     *
     * @param state
     * @param iconViewClickListener 网络异常时, iconView处理点击刷新事件
     * @return
     */
    public BlankPage showBlankPage(int state, View.OnClickListener iconViewClickListener) {
        if (mBlankPage != null) {
            mBlankPage.setPageState(state, iconViewClickListener);
            showBlankPage();
        }
        return mBlankPage;
    }

    public BlankPage hideBlankPage() {
        if (mBlankPage != null) {
            if (mCustomView != null) {
                mCustomView.setVisibility(View.VISIBLE);
            }
            mBlankPage.setVisibility(View.GONE);
        }
        return mBlankPage;
    }

    public void showLoadingView() {
        if (mLoadingView != null) {
            if (mBlankPage != null) {
                mBlankPage.setVisibility(View.GONE);
            }
            if (mCustomView != null) {
                mCustomView.setVisibility(View.INVISIBLE);
            }
            mLoadingView.setVisibility(View.VISIBLE);
            mLoadingView.appearAnim();
        }
    }

    public void hideLoadingView() {
        if (mLoadingView != null) {
            if (mCustomView != null) {
                mCustomView.setVisibility(View.VISIBLE);
            }
            mLoadingView.disappearAnim(null);
            mLoadingView.setVisibility(View.GONE);
        }
    }

    public boolean isShowLoadingView(){
        if (mLoadingView != null) {
            if (mLoadingView.getVisibility() == View.VISIBLE) {
               return true;
            }
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
