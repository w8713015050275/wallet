package com.letv.walletbiz.coupon.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.letv.wallet.common.activity.AccountBaseActivity;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.view.BlankPage;
import com.letv.walletbiz.R;
import com.letv.walletbiz.base.util.Action;
import com.letv.walletbiz.base.util.WalletConstant;
import com.letv.walletbiz.coupon.CouponConstant;
import com.letv.walletbiz.coupon.adapter.CouponListAdapter;
import com.letv.walletbiz.coupon.beans.BaseCoupon;
import com.letv.walletbiz.coupon.beans.CardCouponList;
import com.letv.walletbiz.coupon.beans.CouponListResponseResult;
import com.letv.walletbiz.coupon.utils.CardCouponListTask;
import com.letv.walletbiz.coupon.utils.CouponCommonCallback;
import com.letv.walletbiz.coupon.utils.CouponListLoadTask;
import com.letv.walletbiz.coupon.utils.CouponUtils;
import com.letv.walletbiz.coupon.utils.RecyclerScroller;
import com.letv.walletbiz.movie.MovieTicketConstant;
import com.letv.walletbiz.movie.activity.MovieOrderDetailActivity;
import com.letv.walletbiz.movie.beans.MovieOrder;
import com.letv.walletbiz.movie.utils.MoviePriorityExecutorHelper;

import org.xutils.common.task.PriorityExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import timehop.stickyheader.RecyclerItemClickListener;

/**
 * Created by lijunying on 16-4-15.
 */
public class CouponListActivity extends AccountBaseActivity implements View.OnClickListener, AccountHelper.OnAccountChangedListener {
    private static final String TAG = CouponListActivity.class.getSimpleName();
    private static String ISGOLOGIN = "isGoLogin";
    private RecyclerView mRecyclerView;
    private TextView tvExpiredMsg;
    private LinearLayout viewExpired;
    private CouponListAdapter mAdapter;
    private CouponListLoadTask mCouponLoadTask;
    private CardCouponListTask mCardLoadTask;
    private PriorityExecutor mPriorityExecutor;
    private long last_id_card = -1;
    private long last_id_coupon = -1;
    private int mExpireNum;
    private List<BaseCoupon> mCouponList;
    private List<MovieOrder> mCardList;
    private RecyclerScroller recyclerScroller;
    private static final int PAGE_SIZE_3 = 3;
    private static final int PAGE_SIZE_10 = 10;
    private Intent intent;
    private boolean isPageSuccessLoaded = false;
    private boolean isDataExpire = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerNetWorkReceiver();
        if (savedInstanceState == null || !savedInstanceState.getBoolean(ISGOLOGIN)) {
            AccountHelper.getInstance().loginLetvAccountIfNot(this, null);
        }
        Intent intent = getIntent();
        if (intent != null) {
            String from;
            Uri uri = intent.getData();
            if (uri != null) {
                from = uri.getQueryParameter(WalletConstant.EXTRA_FROM);
            } else {
                from  = intent.getStringExtra(WalletConstant.EXTRA_FROM);
            }
            Action.uploadCouponListExpose(from);
        }

        setContentView(R.layout.coupon_list_activity);
        mPriorityExecutor = MoviePriorityExecutorHelper.getPriorityExecutor();
        initView();
        AccountHelper.getInstance().registerOnAccountChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (AccountHelper.getInstance().isLogin(this)) {
            hideBlankPage();
            if (!isPageSuccessLoaded || isDataExpire) {
                last_id_card = -1;
                last_id_coupon = -1;
                loadCardList(last_id_card, PAGE_SIZE_3);
            }
        } else {
            showNoLoginBlankPage();
        }

    }

    @Override
    protected void onDestroy() {
        AccountHelper.getInstance().unregisterOnAccountChangeListener(this);
        super.onDestroy();
    }

    private void initView() {
        viewExpired = (LinearLayout) findViewById(R.id.viewExpired);
        viewExpired.setOnClickListener(this);
        tvExpiredMsg = (TextView) findViewById(R.id.tvExpiredMsg);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new CouponListAdapter(this);
        mAdapter.setMoreViewOnClickListener(this);
        mRecyclerView.setAdapter(mAdapter);
        recyclerScroller = new RecyclerScroller(layoutManager, new RecyclerScroller.OnScrollListener() {
            @Override
            public void onLoadMore() {
                loadCouponList(last_id_coupon, CouponConstant.COUPON_PARAM_LIMIT_DEFAULT);
            }
        });
        mRecyclerView.addOnScrollListener(recyclerScroller);
        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (mAdapter != null) {
                    Object obj = mAdapter.getItem(position);
                    if (obj != null) {
                        if (obj instanceof BaseCoupon) {
                            intent = new Intent(CouponListActivity.this, CouponDetailActivity.class);
                            intent.putExtra(CouponConstant.EXTRA_COUPON_BEAN, (BaseCoupon) obj);

                        } else if (obj instanceof MovieOrder) {
                            intent = new Intent(CouponListActivity.this, MovieOrderDetailActivity.class);
                            intent.putExtra(MovieTicketConstant.EXTRA_MOVIE_ORDER_NUM, ((MovieOrder) obj).order_no);
                        }
                        isPageSuccessLoaded = false; //返回页面，刷新
                        startActivity(intent);

                    }
                }
            }
        }));
    }

    @Override
    protected void onNetWorkChanged(boolean isNetworkAvailable) {
        if (isNetworkAvailable && AccountHelper.getInstance().isLogin(this)) {
            if (!isPageSuccessLoaded || isDataExpire) {
                last_id_card = -1;
                last_id_coupon = -1;
                loadCardList(last_id_card, PAGE_SIZE_3);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState != null) {
            outState.putBoolean(ISGOLOGIN, !AccountHelper.getInstance().isLogin(this));
        }
    }

    private void loadCouponList(long last_id, int limit) {
        if (isNetworkAvailable()) {
            if (mCouponLoadTask == null) {

                mCouponLoadTask = new CouponListLoadTask(this, last_id, limit);
                mCouponLoadTask.setResponseCallback(mCouponListCallback);
                mPriorityExecutor.execute(mCouponLoadTask);
            }
        } else if ((mCouponList != null && mCouponList.size() > 0) || (mCardList != null && mCardList.size() > 0)) {
            CouponUtils.showToast(this, R.string.empty_no_network);
        } else {
            handleError(CouponCommonCallback.ERROR_NO_NETWORK);
        }

    }

    private void loadCardList(long last_id, int limit) { //卡劵
        if (isNetworkAvailable()) {
            if (mCardLoadTask == null) {
                if ((mCardList == null || mCardList.size() <= 0) && (mCouponList == null || mCouponList.size() <= 0))
                    showLoadingView();
                mCardLoadTask = new CardCouponListTask(this, mCardListCallback);
                mCardLoadTask.setParams(MovieOrder.MOVIE_TICKET_PROGRESS_UNCONSUMED, last_id, -1, limit);
                mPriorityExecutor.execute(mCardLoadTask);
            }
        } else if ((mCardList != null && mCardList.size() > 0) || (mCouponList != null && mCouponList.size() > 0)) {
            CouponUtils.showToast(this, R.string.empty_no_network);
        } else {
            handleError(CouponCommonCallback.ERROR_NO_NETWORK);
        }

    }

    private CouponCommonCallback<CardCouponList> mCardListCallback = new CouponCommonCallback<CardCouponList>() {
        @Override
        public void onLoadFinished(CardCouponList result, int errorCode) {
            if (errorCode == CouponCommonCallback.NO_ERROR) {
                if (result != null && result.list != null && result.list.length > 0) {
                    List<MovieOrder> temp = new ArrayList<>(Arrays.asList(result.list));
                    if (last_id_card > 0 && mCardList != null && mCardList.size() > 0) { //加载更多
                        mCardList.addAll(temp);
                        mAdapter.setCardData(mCardList);
                    } else {
                        mCardList = (temp);
                        loadCouponList(last_id_coupon, CouponConstant.COUPON_PARAM_LIMIT_DEFAULT); //加载优惠劵首页
                    }

                    last_id_card = temp.get(temp.size() - 1).rank_id;

                    if (temp.size() < result.limit || !result.next_more) {
                        mAdapter.enableLoadMoreCard(false); //返回数据不满一页 禁用加载更多
                    }

                } else if (mCardList != null && mCardList.size() > 0) {  //没有更多
                    mAdapter.enableLoadMoreCard(false);
                } else {
                    //卡劵为空 ，加载优惠卷
                    loadCouponList(last_id_coupon, CouponConstant.COUPON_PARAM_LIMIT_DEFAULT);
                }

            } else if (errorCode == CouponCommonCallback.ERROR_NETWORK) {
                if ((mCardList != null && mCardList.size() > 0) || (mCouponList != null && mCouponList.size() > 0)) {
                    CouponUtils.showToast(CouponListActivity.this, R.string.empty_network_error);
                } else {
                    //卡劵加载出错， 加载优惠劵
                    loadCouponList(last_id_coupon, CouponConstant.COUPON_PARAM_LIMIT_DEFAULT); //加载优惠卷首页
                }

            } else if (errorCode == CouponCommonCallback.ERROR_NO_NETWORK) {
                if ((mCardList != null && mCardList.size() > 0) || (mCouponList != null && mCouponList.size() > 0)) {
                    CouponUtils.showToast(CouponListActivity.this, R.string.empty_no_network);
                } else {
                    hideLoadingView();
                    handleError(CouponCommonCallback.ERROR_NO_NETWORK);
                }
            } else {
                hideLoadingView();
            }

            mCardLoadTask = null;
        }
    };

    private CouponCommonCallback<CouponListResponseResult> mCouponListCallback = new CouponCommonCallback<CouponListResponseResult>() {
        @Override
        public void onLoadFinished(CouponListResponseResult result, int errorCode) {
            if (isShowLoadingView()) hideLoadingView();

            if (errorCode == CouponCommonCallback.NO_ERROR) {
                if (result != null && result.list != null && result.list.length > 0) {
                    List<BaseCoupon> temp = new ArrayList<>(Arrays.asList(result.list));
                    if (last_id_coupon > 0 && mCouponList != null && mCouponList.size() > 0) {
                        mCouponList.addAll(temp);
                        mAdapter.setCouponData(mCouponList);
                    } else {
                        mCouponList = temp;
                        mAdapter.setData(mCouponList, mCardList); //优惠劵和卡劵首页一起加载
                        isPageSuccessLoaded = true;
                        isDataExpire = false;
                    }

                    last_id_coupon = temp.get(temp.size() - 1).getRank_id();

                } else if (mCouponList != null && mCouponList.size() > 0) {
                    recyclerScroller.setEnableLoadMore(false);
                } else if (mCouponList == null || mCouponList.size() <= 0) {
                    if (mCardList != null && mCardList.size() > 0) { //获取优惠劵首页为空，更新卡卷
                        mAdapter.setCardData(mCardList);
                        isPageSuccessLoaded = true;
                        isDataExpire = false;
                    } else {
                        handleError(CouponCommonCallback.ERROR_NO_DATA);
                    }

                }
                mExpireNum = result == null ? 0 : result.expire_num;
                if (mExpireNum > 0) {
                    viewExpired.setVisibility(View.VISIBLE);
                    tvExpiredMsg.setText(getResources().getQuantityString(R.plurals.coupon_list_expired_msg_count, mExpireNum, mExpireNum));
                } else {
                    viewExpired.setVisibility(View.GONE);
                }

            } else if (errorCode == CouponCommonCallback.ERROR_NETWORK) {
                if ((mCouponList != null && mCouponList.size() > 0) || (mCardList != null && mCardList.size() > 0)) {
                    CouponUtils.showToast(CouponListActivity.this, R.string.empty_network_error);
                } else {
                    handleError(CouponCommonCallback.ERROR_NETWORK);
                }

            } else if (errorCode == CouponCommonCallback.ERROR_NO_NETWORK) {
                if ((mCouponList != null && mCouponList.size() > 0) || (mCardList != null && mCardList.size() > 0)) {
                    CouponUtils.showToast(CouponListActivity.this, R.string.empty_no_network);
                } else {
                    handleError(CouponCommonCallback.ERROR_NO_NETWORK);
                }
            }
            mCouponLoadTask = null;
        }
    };

    private void handleError(int errorCode) {
        if (errorCode == CouponCommonCallback.ERROR_NO_NETWORK) {
            showBlankPage(BlankPage.STATE_NO_NETWORK);
        } else if (errorCode == CouponCommonCallback.ERROR_NETWORK) {
            showBlankPage(BlankPage.STATE_NETWORK_ABNORMAL).getIconView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO 重新加载
                    loadCardList(last_id_card, PAGE_SIZE_3);
                }
            });
        } else if (errorCode == CouponCommonCallback.ERROR_NO_DATA) {
            BlankPage blankPage = showBlankPage();
            String str = " ";
            str = getString(R.string.coupon_lis_no_data);
            blankPage.setCustomPage(str, BlankPage.Icon.NO_ACCESS);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.viewExpired:
                Intent intent = new Intent(this, CouponExpiredListActivity.class);
                intent.putExtra(CouponConstant.EXTRA_COUPON_EXPIRED_MSG_CHECK, true);
                startActivityForResult(intent, CouponConstant.REQUEST_CHECK_EXPIRED);
                break;
            case R.id.tv_coupon_list_more:
                loadCardList(last_id_card, PAGE_SIZE_10);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == CouponConstant.REQUEST_CHECK_EXPIRED) {
            viewExpired.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_coupon_expired, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (AccountHelper.getInstance().isLogin(this)) {
            if (item.getItemId() == R.id.action_coupon_expired) {
                startActivityForResult(new Intent(this, CouponExpiredListActivity.class), CouponConstant.REQUEST_CHECK_EXPIRED);
            }

        } else {
            CouponUtils.showToast(getApplicationContext(), R.string.empty_no_login);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAccountLogin() {
        isDataExpire = true;
    }

    @Override
    public void onAccountLogout() {
        isDataExpire = true;
    }
}
