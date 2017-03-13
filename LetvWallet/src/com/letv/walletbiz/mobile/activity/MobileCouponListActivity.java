package com.letv.walletbiz.mobile.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.letv.wallet.common.activity.AccountBaseActivity;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.CommonCallback;
import com.letv.wallet.common.util.ExecutorHelper;
import com.letv.wallet.common.view.BlankPage;
import com.letv.wallet.common.view.DividerItemDecoration;
import com.letv.walletbiz.R;
import com.letv.walletbiz.base.activity.ActivityConstant;
import com.letv.walletbiz.mobile.MobileConstant;
import com.letv.walletbiz.mobile.beans.CouponBean;
import com.letv.walletbiz.mobile.beans.CouponListBean;
import com.letv.walletbiz.mobile.ui.CouponListAdapter;
import com.letv.walletbiz.mobile.util.CouponListTask;

/**
 * Created by changjiajie on 16-1-27.
 */
public class MobileCouponListActivity extends AccountBaseActivity implements CouponListAdapter.OnItemClickListener, CommonCallback {

    private static final int PRODUCT_ID = -1;
    private RecyclerView mCouponListV;
    private LinearLayoutManager mLayoutManager;
    private CouponListAdapter mAdapter;

    private String promptNoRecordStr;

    private CouponListTask mCouponAsyncT;

    private CouponBean[] mCouponList;
    private long mUcouponId;
    private String mSkus;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerNetWorkReceiver();
        setContentView(R.layout.mobile_coupon_list);
        iniV();
    }

    private void queryCouponInfo(String skus) {
        if (!isNetworkAvailable()) {
            promptNoNetWork();
            return;
        }
        String uToken = AccountHelper.getInstance().getToken(MobileCouponListActivity.this);
        showLoadingView();
        mCouponAsyncT = new CouponListTask(MobileCouponListActivity.this, this, uToken, skus);
        ExecutorHelper.getExecutor().runnableExecutor(mCouponAsyncT);
    }

    @Override
    public void onLoadFinished(Object result, int errorCode) {
        hideLoadingView();
        if (result != null) {
            CouponListBean<CouponBean> couponListBean = (CouponListBean) result;
            setData(couponListBean);
        } else {
            if (mCouponList == null) {
                setData(null);
            } else {
                if (!isNetworkAvailable()) {
                    promptNoNetWork();
                }
            }
        }
        mCouponAsyncT = null;
    }

    @Override
    protected void onNetWorkChanged(boolean isNetworkAvailable) {
        if (isNetworkAvailable && (mCouponList == null || mCouponList.length < 0)) {
            queryCouponInfo(mSkus);
        }
    }

    private void iniV() {
        promptNoRecordStr = getResources().getString(R.string.empty_no_coupon);
        Bundle bundle = getIntent().getExtras();
        CouponListBean<CouponBean> mCouponListBean = (CouponListBean<CouponBean>) bundle.getSerializable(MobileConstant.PARAM.COUPONLIST_KEY);
        mUcouponId = bundle.getLong(MobileConstant.PARAM.COUPON_ID_KEY);
        mSkus = bundle.getString(MobileConstant.PARAM.SKU_SN);
        setData(mCouponListBean);
    }

    private void setData(CouponListBean<CouponBean> couponListBean) {
        if (couponListBean != null && couponListBean.list != null && couponListBean.list.length > 0) {
            mCouponList = couponListBean.list;
            findViewById();
            mAdapter = new CouponListAdapter(getBaseContext(), mCouponList, mUcouponId);
            mAdapter.setOnItemClickListener(MobileCouponListActivity.this);
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
        Intent intent = new Intent(MobileCouponListActivity.this, MobileOrderConfirmationActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt(MobileConstant.PARAM.COUPON_LIST_COUNT_KEY, mCouponList.length);
        bundle.putSerializable(MobileConstant.PARAM.COUPON_DATA_KEY, couponBean);
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        finish();
    }

}