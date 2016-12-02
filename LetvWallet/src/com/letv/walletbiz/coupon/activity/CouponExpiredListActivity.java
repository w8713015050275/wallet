package com.letv.walletbiz.coupon.activity;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.LinearLayout;

import com.letv.wallet.common.activity.AccountBaseActivity;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.view.BlankPage;
import com.letv.walletbiz.R;
import com.letv.walletbiz.coupon.CouponConstant;
import com.letv.walletbiz.coupon.adapter.CouponListAdapter;
import com.letv.walletbiz.coupon.fragment.CouponExpiredFragment;

/**
 * Created by lijunying on 16-4-19.
 */
public class CouponExpiredListActivity extends AccountBaseActivity {
    private static final String TAG = CouponExpiredListActivity.class.getSimpleName();
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private ViewPagerAdpter mViewPagerAdapter;
    private String[] tabArrays;
    private boolean isFromMsgCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.coupon_list_expired_activity);
        isFromMsgCheck = getIntent().getBooleanExtra(CouponConstant.EXTRA_COUPON_EXPIRED_MSG_CHECK, false);
        intiView();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //super.onSaveInstanceState(outState);
    }

    private void intiView() {
        tabArrays = getResources().getStringArray(R.array.coupon_expired_tab_array);
        mTabLayout = (TabLayout) findViewById(R.id.tab_indicator);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPagerAdapter = new ViewPagerAdpter(getSupportFragmentManager(), tabArrays);
        mViewPager.setAdapter(mViewPagerAdapter);
        mTabLayout.setTabMode(TabLayout.MODE_FIXED);
        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        mTabLayout.setupWithViewPager(mViewPager);
        setTabMarginHorizontal((int) getResources().getDimension(R.dimen.tab_margin_horizontal));
        if (isFromMsgCheck) {
            mViewPager.setCurrentItem(1, true);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (AccountHelper.getInstance().isLogin(this)) {
            hideBlankPage();

        } else {
            showBlankPage(BlankPage.STATE_NO_LOGIN);
        }
    }

    @Override
    public boolean hasBlankAndLoadingView() {
        return true;
    }

    static class ViewPagerAdpter extends FragmentPagerAdapter {
        String[] mTabName;
        CouponExpiredFragment cardCouponFragment;
        CouponExpiredFragment couponFragment;

        public ViewPagerAdpter(FragmentManager fm, String[] mTabName) {
            super(fm);
            this.mTabName = mTabName;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            if (position == 0) {
                if (cardCouponFragment == null) {
                    cardCouponFragment = CouponExpiredFragment.newInstance(CouponListAdapter.VIEW_TYPE_CARD_ITEM);
                }
                fragment = cardCouponFragment;
            } else if (position == 1) {
                if (couponFragment == null) {
                    couponFragment = CouponExpiredFragment.newInstance(CouponListAdapter.VIEW_TYPE_COUPON_ITEM);
                }
                fragment = couponFragment;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return mTabName.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTabName[position];
        }
    }

    private void setTabMarginHorizontal(int margin_in_dp) {
        for (int i = 0; i < tabArrays.length; i++) {
            LinearLayout layout = ((LinearLayout) ((LinearLayout) mTabLayout.getChildAt(0)).getChildAt(i));
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) layout.getLayoutParams();
            layoutParams.leftMargin = margin_in_dp;
            layoutParams.rightMargin = margin_in_dp;
            layout.setLayoutParams(layoutParams);
        }
    }

}
