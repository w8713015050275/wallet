package com.letv.walletbiz.main.fragment;

import android.support.v7.app.ActionBar;

import com.letv.wallet.common.fragment.BaseFragment;
import com.letv.walletbiz.base.activity.BaseWalletFragmentActivity;

/**
 * Created by zhuchuntao on 16-12-21.
 */
public abstract class MainFragment extends BaseFragment {

    @Override
    public void onStart() {
        super.onStart();
        changeActionbar();
    }

    public abstract void startLoadData();

    public abstract void onNetWorkChanged(boolean isNetworkAvailable);

    public abstract boolean displayActionbar();

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        changeActionbar();
        //执行异步加载任务，第二次显示的时候要重新加载数据
        if (!hidden) {
            startLoadData();
        }
    }

    //更改actionbar的显示状态
    private void changeActionbar() {
        BaseWalletFragmentActivity a = (BaseWalletFragmentActivity) getActivity();
        ActionBar ab = a.getSupportActionBar();
        if (displayActionbar()) {
            if (!ab.isShowing())
                ab.show();
        } else {
            if (ab.isShowing())
                ab.hide();
        }
    }
}
