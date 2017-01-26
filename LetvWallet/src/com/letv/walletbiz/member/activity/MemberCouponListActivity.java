package com.letv.walletbiz.member.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.letv.wallet.common.activity.AccountBaseActivity;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.ExecutorHelper;
import com.letv.wallet.common.util.LogHelper;
import com.letv.wallet.common.view.BlankPage;
import com.letv.wallet.common.view.DividerItemDecoration;
import com.letv.walletbiz.R;
import com.letv.walletbiz.base.activity.ActivityConstant;
import com.letv.walletbiz.member.MemberConstant;
import com.letv.walletbiz.member.adapters.CouponListAdapter;
import com.letv.walletbiz.member.beans.CouponBean;
import com.letv.walletbiz.member.task.CouponAvailableTask;
import com.letv.walletbiz.member.util.MemberCommonCallback;


/**
 * Created by zhanghuancheng on 16-11-22.
 */
public class MemberCouponListActivity extends AccountBaseActivity implements CouponListAdapter.OnItemClickListener, MemberCommonCallback<CouponBean[]> {
    private String promptNoRecordStr;
    private long mUcouponId;
    private CouponBean[] mCouponList;
    private CouponListAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private RecyclerView mCouponListV;
    private CouponAvailableTask mCouponLoadTask;
    private String mSkus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerNetWorkReceiver();
        setContentView(R.layout.member_coupon_list);
        initView();
    }

    private void queryCouponInfo() {
        if (!isNetworkAvailable()) {
            promptNoNetWork();
            return;
        }
        String uToken = AccountHelper.getInstance().getToken(MemberCouponListActivity.this);
        showLoadingView();
        if (mCouponLoadTask == null) {
            mCouponLoadTask = new CouponAvailableTask(uToken, mSkus, this);
        }
        ExecutorHelper.getExecutor().runnableExecutor(mCouponLoadTask);
    }

    @Override
    protected void onNetWorkChanged(boolean isNetworkAvailable) {
        if (isNetworkAvailable && (mCouponList == null || mCouponList.length < 0)) {
            queryCouponInfo();
        }
    }

    private void initView() {
        promptNoRecordStr = getResources().getString(R.string.empty_no_coupon);
        Bundle bundle = getIntent().getExtras();
        CouponBean[] mCouponBeans = (CouponBean[]) bundle.getSerializable(MemberConstant.PARAM.COUPONLIST_KEY);
        mUcouponId = bundle.getLong(MemberConstant.PARAM.COUPON_ID_KEY);
        mSkus = bundle.getString(MemberConstant.PARAM.MEMBER_SKUS);
        setData(mCouponBeans);
    }

    private void setData(CouponBean[] couponBeans) {
        if (couponBeans != null && couponBeans.length > 0) {
            mCouponList = couponBeans;
            findViewById();
            if (mUcouponId == 0L) {
                mUcouponId = mCouponList[0].ucoupon_id;
            }
            mAdapter = new CouponListAdapter(getBaseContext(), mCouponList, mUcouponId);
            mAdapter.setOnItemClickListener(MemberCouponListActivity.this);
            mLayoutManager =
                    new LinearLayoutManager(getBaseContext());
            mCouponListV.setLayoutManager(mLayoutManager);
            DividerItemDecoration dividerItemDecoration =
                    new DividerItemDecoration(getBaseContext(), getResources().getColor(R.color.colorDividerLineBg),
                            DividerItemDecoration.VERTICAL_LIST, getResources().getDimensionPixelSize(R.dimen.divider_width));
            mCouponListV.addItemDecoration(dividerItemDecoration);
            mCouponListV.setAdapter(mAdapter);
        } else {
            if (mCouponList == null) {
                showErrorBlankPage(ActivityConstant.RETURN_STATUS.FAIL_STATE_N0_RECORD);
            }
        }
    }

    private void showErrorBlankPage(int failState) {
        switch (failState) {
            case ActivityConstant.RETURN_STATUS.FAIL_STATE_N0_RECORD:
                showBlankPage().setCustomPage(promptNoRecordStr, BlankPage.Icon.NO_ACCESS);
                break;
            default: {
                showBlankPage(failState);
            }
            break;
        }
    }

    private void findViewById() {
        mCouponListV = (RecyclerView) findViewById(R.id.coupon_rclerv);
    }

    @Override
    public void onItemClick(CouponBean couponBean) {
        if (couponBean == null) return;
        Intent intent = new Intent(MemberCouponListActivity.this, MemberOrderConfirmActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt(MemberConstant.PARAM.COUPON_LIST_COUNT_KEY, mCouponList.length);
        bundle.putSerializable(MemberConstant.PARAM.COUPON_DATA_KEY, couponBean);
        bundle.putLong(MemberConstant.PARAM.COUPON_ID_KEY, couponBean.ucoupon_id);
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onLoadFinished(CouponBean[] result, int errorCode, boolean needUpdate) {
        hideLoadingView();
        if (result != null) {
            CouponBean[] couponBeans = result;
            setData(couponBeans);
        } else {
            if (mCouponList == null) {
                setData(null);
            } else {
                if (!isNetworkAvailable()) {
                    promptNoNetWork();
                }
            }
        }
        mCouponLoadTask = null;
    }
}

