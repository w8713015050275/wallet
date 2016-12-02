package com.letv.walletbiz.order.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.letv.wallet.common.activity.BaseFragmentActivity;
import com.letv.walletbiz.R;
import com.letv.walletbiz.base.activity.BaseWalletFragmentActivity;
import com.letv.walletbiz.order.fragment.TotalOrderListFragment;

/**
 * Created by changjiajie on 16-1-27.
 */
public class TotalOrderListActivity extends BaseWalletFragmentActivity {
    private static String TAG = "TotalOrder";
    private FragmentTransaction mTransaction;
    private TotalOrderListFragment mOrderListFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_v);
        iniV();
    }

    @Override
    public boolean hasBlankAndLoadingView() {
        return false;
    }

    private void iniV() {
        FragmentManager mFragmentManager = getSupportFragmentManager();
        mTransaction = mFragmentManager.beginTransaction();
        mOrderListFragment = (TotalOrderListFragment) mFragmentManager.findFragmentByTag(TAG);
        if (mOrderListFragment == null) {
            mOrderListFragment = new TotalOrderListFragment();
        }
        mTransaction.replace(R.id.order_list_content, mOrderListFragment, TAG);
        mTransaction.commit();
    }
}