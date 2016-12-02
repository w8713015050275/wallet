package com.letv.wallet.common.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.letv.shared.widget.LeLoadingView;
import com.letv.wallet.common.R;
import com.letv.wallet.common.util.NetworkHelper;
import com.letv.wallet.common.view.BlankPage;

/**
 * Created by liuliang on 15-12-29.
 */
public abstract class BaseFragment extends Fragment {
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected boolean registerNetWorkReceiver() {
        if (getContext() == null) {
            return false;
        }
        isNetworkAvailable = NetworkHelper.isNetworkAvailable();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        getContext().registerReceiver(mNetWokReceiver, filter);
        isRegisteredNetWork = true;
        return true;
    }

    public BlankPage getBlankPage() {
        return mBlankPage;
    }

    @Override
    public void onDestroy() {
        try {
            if (isRegisteredNetWork && getContext() != null) {
                getContext().unregisterReceiver(mNetWokReceiver);
            }
        } catch (Exception e) {
        }
        isRegisteredNetWork = false;
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (hasBlankAndLoadingView()) {
            mLayoutContainer = (FrameLayout) inflater.inflate(R.layout.base_blank_loading_layout, container, false);
            initBaseView();
            View view = onCreateCustomView(inflater, mLayoutContainer, savedInstanceState);
            if (view != null) {
                mCustomView = view;
                mLayoutContainer.addView(view, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                if ((mBlankPage != null && mBlankPage.getVisibility() == View.VISIBLE)
                        || (mLoadingView != null && mLoadingView.getVisibility() == View.VISIBLE)) {
                    mCustomView.setVisibility(View.GONE);
                } else {
                    mCustomView.setVisibility(View.VISIBLE);
                    mCustomView.bringToFront();
                }
            }
            return mLayoutContainer;
        } else {
            return onCreateCustomView(inflater, container, savedInstanceState);
        }
    }

    public boolean isNetworkAvailable() {
        if (!isRegisteredNetWork) {
            isNetworkAvailable = NetworkHelper.isNetworkAvailable();
        }
        return isNetworkAvailable;
    }

    public abstract View onCreateCustomView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

    /**
     * 是否增加错误和loading页面
     *
     * @return
     */
    public abstract boolean hasBlankAndLoadingView();

    /**
     * 工作在UI线程,必须首先调用 registerNetWorkReceiver()
     */
    protected void onNetWorkChanged(boolean isNetworkAvailable) {
    }

    private void initBaseView() {
        mBlankPage = (BlankPage) mLayoutContainer.findViewById(R.id.base_blank_page);
        mLoadingView = (LeLoadingView) mLayoutContainer.findViewById(R.id.base_loading);
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
                mCustomView.setVisibility(View.GONE);
            }
            mBlankPage.setVisibility(View.VISIBLE);
            mBlankPage.bringToFront();
        }
        return mBlankPage;
    }

    public BlankPage showBlankPage(int state) {
        return showBlankPage(state, null);
    }

    /**
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
            mBlankPage.setVisibility(View.GONE);
            if (mCustomView != null) {
                mCustomView.setVisibility(View.VISIBLE);
                mCustomView.bringToFront();
            }
        }
        return mBlankPage;
    }

    public void showLoadingView() {
        if (mLoadingView != null) {
            if (mBlankPage != null) {
                mBlankPage.setVisibility(View.GONE);
            }
            if (mCustomView != null) {
                mCustomView.setVisibility(View.GONE);
            }
            mLoadingView.setVisibility(View.VISIBLE);
            mLoadingView.bringToFront();
            mLoadingView.appearAnim();
        }
    }

    public void hideLoadingView() {
        if (mLoadingView != null) {
            mLoadingView.disappearAnim(null);
            mLoadingView.setVisibility(View.GONE);
            if (mCustomView != null) {
                mCustomView.setVisibility(View.VISIBLE);
                mCustomView.bringToFront();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
