package com.letv.walletbiz.main.fragment;

import com.letv.wallet.common.fragment.BaseFragment;

/**
 * Created by zhuchuntao on 16-12-21.
 */
public abstract class MainFragment extends BaseFragment {


   public abstract void startLoadData();

   public abstract void onNetWorkChanged(boolean isNetworkAvailable);

}
