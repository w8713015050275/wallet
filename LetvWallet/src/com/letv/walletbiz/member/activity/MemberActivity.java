package com.letv.walletbiz.member.activity;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.LogHelper;
import com.letv.wallet.common.util.NetworkHelper;
import com.letv.wallet.common.util.PriorityExecutorHelper;
import com.letv.wallet.common.view.BlankPage;
import com.letv.walletbiz.R;
import com.letv.walletbiz.base.activity.BaseWalletFragmentActivity;
import com.letv.walletbiz.member.beans.MemberTypeListBean;
import com.letv.walletbiz.member.fragments.MemberFragment;
import com.letv.walletbiz.member.task.MemberTypeListTask;
import com.letv.walletbiz.member.util.MemberCommonCallback;

import org.xutils.common.task.PriorityExecutor;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class MemberActivity extends BaseWalletFragmentActivity {

    private ArrayList<String> mTabs = new ArrayList<>();
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private ViewPagerAdapter mViewPagerAdapter;

    private String mPromptNetConnectionFail;

    private PriorityExecutor mExecuter;
    private MemberTypeListTask mMemberTypeTask;

    private View.OnClickListener mRetryClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            loadData();
        }

    };
    private MemberTypeListBean.MemberTypeBean[] mMemberBeanList;
    private ActionBar mActionBar;
    private boolean mGotoOrderList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerNetWorkReceiver();
        setContentView(R.layout.member_activity);
        initView();
    }

    private void initView() {
        mActionBar = getSupportActionBar();
        setTitle(R.string.member_tool_bar);
        initActionBar();

        mExecuter = PriorityExecutorHelper.getPriorityExecutor();
        mPromptNetConnectionFail = getString(R.string.mobile_prompt_net_connection_fail);
        mTabLayout = (TabLayout) findViewById(R.id.tab_indicator);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);

        mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mViewPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        setTabMarginHorizontal((int) getResources().getDimension(R.dimen.tab_margin_horizontal));
        loadData();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void loadData() {
        if (mTabs == null || mTabs.size() == 0) {
            showLoadingView();
        }
        if (mMemberTypeTask == null) {
            mMemberTypeTask = new MemberTypeListTask(MemberActivity.this, new MemberCommonCallback<MemberTypeListBean.MemberTypeBean[]>() {
                @Override
                public void onLoadFinished(MemberTypeListBean.MemberTypeBean[] result, int errorCode, boolean needUpdate) {
                    hideLoadingView();
                    hideBlankPage();
                    if (errorCode == MemberCommonCallback.NO_ERROR) {
                        if (result != null && result.length > 0) {
                            Arrays.sort(result);
                            mMemberBeanList = result;
                            mTabs.clear();
                            for (MemberTypeListBean.MemberTypeBean typeBean : result) {
                                mTabs.add(typeBean.name);
                            }
                            mViewPagerAdapter.setData(mTabs);
                            setTabMarginHorizontal((int) getResources().getDimension(R.dimen.tab_margin_horizontal));
                        } else if (!isNetworkAvailable()) {
                            showBlankPage(BlankPage.STATE_NO_NETWORK);
                        }
                    } else if (!isNetworkAvailable() || errorCode == MemberCommonCallback.ERROR_NO_NETWORK) {
                        showBlankPage(BlankPage.STATE_NO_NETWORK);
                    } else if (errorCode == MemberCommonCallback.ERROR_NETWORK) {
                        showBlankPage(BlankPage.STATE_NETWORK_ABNORMAL, mRetryClickListener);
                    }
                    mMemberTypeTask = null;
                }
            });
        }
        mExecuter.execute(mMemberTypeTask);
    }

    private void initActionBar() {

    }

    @Override
    public boolean hasBlankAndLoadingView() {
        return true;
    }

    @Override
    protected void onNetWorkChanged(boolean isNetworkAvailable) {
        if (NetworkHelper.isNetworkAvailable()) {
            loadData();
        }
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {
        private ArrayList<String> mTabNames;
        private ArrayList<MemberFragment> mFragmentList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mTabNames == null ? 0 : mTabNames.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTabNames.get(position);
        }

        public void setData(ArrayList<String> data) {
            if (mTabNames != null && data != null) {
                if (mTabNames.equals(data)) {
                    return;
                }
            }
            mTabNames = data;
            mFragmentList.clear();
            for (int i = 0; i < mTabNames.size(); i++) {
                if (mMemberBeanList != null && mMemberBeanList.length == mTabNames.size()) {
                    mFragmentList.add(MemberFragment.newInstance(mMemberBeanList[i]));
                }
            }
            notifyDataSetChanged();
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }

    private void setTabMarginHorizontal(int margin_in_dp) {
        if (mTabs == null || mTabLayout == null) return;
        for (int i = 0; i < mTabs.size(); i++) {
            LinearLayout layout = ((LinearLayout) ((LinearLayout) mTabLayout.getChildAt(0)).getChildAt(i));
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) layout.getLayoutParams();
            layoutParams.leftMargin = margin_in_dp;
            layoutParams.rightMargin = margin_in_dp;
            layout.setLayoutParams(layoutParams);
        }
    }

    public void showNetFailToast() {
        Toast.makeText(MemberActivity.this, mPromptNetConnectionFail, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_member, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_list_orders) {
            goToOrderList();
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean goToOrderList() {
        mGotoOrderList = true;
        if (AccountHelper.getInstance().loginLetvAccountIfNot(this, null)) {
            mGotoOrderList = false;
            startOrderListActivity();
            return true;
        }
        return false;
    }

    private void startOrderListActivity() {
        Intent intent = new Intent(MemberActivity.this, MemberOrderListActivity.class);
        startActivity(intent);
    }
}
