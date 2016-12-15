package com.letv.walletbiz.member.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.letv.walletbiz.R;
import com.letv.walletbiz.base.activity.BaseWalletFragmentActivity;
import com.letv.walletbiz.member.fragments.MemberOrderListFragment;

/**
 * Created by zhanghuancheng on 16-11-24.
 */
public class MemberOrderListActivity extends BaseWalletFragmentActivity {
    private static String TAG = "TotalOrder";
    private FragmentTransaction mTransaction;
    private MemberOrderListFragment mOrderListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_v);
        initView();
    }

    @Override
    public boolean hasBlankAndLoadingView() {
        return false;
    }

    private void initView() {
        FragmentManager mFragmentManager = getSupportFragmentManager();
        mTransaction = mFragmentManager.beginTransaction();
        mOrderListFragment = (MemberOrderListFragment) mFragmentManager.findFragmentByTag(TAG);
        if (mOrderListFragment == null) {
            mOrderListFragment = new MemberOrderListFragment();
        }
        mTransaction.replace(R.id.order_list_content, mOrderListFragment, TAG);
        mTransaction.commit();
    }
}

