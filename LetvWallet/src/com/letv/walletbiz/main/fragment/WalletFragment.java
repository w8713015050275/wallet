package com.letv.walletbiz.main.fragment;

import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.AppUtils;
import com.letv.wallet.common.util.CommonConstants;
import com.letv.wallet.common.util.NetworkHelper;
import com.letv.wallet.common.util.SharedPreferencesHelper;
import com.letv.wallet.common.view.BlankPage;
import com.letv.walletbiz.R;
import com.letv.walletbiz.base.activity.ActivityConstant;
import com.letv.walletbiz.base.util.Action;
import com.letv.walletbiz.base.util.WalletConstant;
import com.letv.walletbiz.base.widget.MainTopButton;
import com.letv.walletbiz.coupon.CouponConstant;
import com.letv.walletbiz.coupon.activity.CouponDetailActivity;
import com.letv.walletbiz.coupon.beans.BaseCoupon;
import com.letv.walletbiz.coupon.beans.CardCouponList;
import com.letv.walletbiz.coupon.beans.CouponListResponseResult;
import com.letv.walletbiz.coupon.utils.CardCouponListTask;
import com.letv.walletbiz.coupon.utils.CouponCheckNewTask;
import com.letv.walletbiz.coupon.utils.CouponCommonCallback;
import com.letv.walletbiz.coupon.utils.CouponListLoadTask;
import com.letv.walletbiz.main.BannerTask;
import com.letv.walletbiz.main.MainAdapter;
import com.letv.walletbiz.main.MainPanelHelper;
import com.letv.walletbiz.main.MainServiceTask;
import com.letv.walletbiz.main.MainTopTask;
import com.letv.walletbiz.main.WalletMainWebActivity;
import com.letv.walletbiz.main.bean.WalletBannerListBean;
import com.letv.walletbiz.main.bean.WalletServiceListBean;
import com.letv.walletbiz.main.bean.WalletTopListBean;
import com.letv.walletbiz.movie.MovieTicketConstant;
import com.letv.walletbiz.movie.activity.MovieOrderDetailActivity;
import com.letv.walletbiz.movie.beans.MovieOrder;

import org.xutils.common.task.PriorityExecutor;

import java.io.IOException;

import timehop.stickyheader.RecyclerItemClickListener;

/**
 * Created by zhuchuntao on 16-12-21.
 */
public class WalletFragment extends MainFragment {
    private PriorityExecutor mExecutor = new PriorityExecutor(3);
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private MainAdapter mAdapter;
    private AccountHelper accountHelper = AccountHelper.getInstance();

    private MainTopButton oneButton;
    private MainTopButton twoButton;
    private MainTopButton threeButton;
    private LinearLayout topLayout;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateCustomView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_tab_wallet, null);

        topLayout = (LinearLayout) view.findViewById(R.id.wallet_top_layout);

        oneButton = (MainTopButton) view.findViewById(R.id.wallet_top_one);
        twoButton = (MainTopButton) view.findViewById(R.id.wallet_top_two);
        threeButton = (MainTopButton) view.findViewById(R.id.wallet_top_three);


        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);

        mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), new RecyclerItemClickListener.OnItemClickListener() {

            @Override
            public void onItemClick(View view, int position) {
                if (position == mAdapter.POSITION_BANNER || position == mAdapter.POSITION_SERVICE) {
                    return;
                }
                Object item = mAdapter.getItem(position);
                if (item == null) {
                    return;
                }
                if (item instanceof MovieOrder) {
                    MovieOrder order = (MovieOrder) item;
                    Intent intent = new Intent(getContext(), MovieOrderDetailActivity.class);
                    intent.putExtra(MovieTicketConstant.EXTRA_MOVIE_ORDER_NUM, order.order_no);
                    startActivity(intent);
                } else if (item instanceof BaseCoupon) {
                    BaseCoupon coupon = (BaseCoupon) item;
                    Intent intent = new Intent(getContext(), CouponDetailActivity.class);
                    intent.putExtra(CouponConstant.EXTRA_COUPON_BEAN, coupon);
                    startActivity(intent);
                }
            }
        }));
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE && mAdapter != null && mAdapter.hasCoupon()) {
                    if (hasMoreCoupon
                            && mLayoutManager.findLastVisibleItemPosition() >= (mAdapter.getItemCount() - 1)) {
                        loadMoreCoupon();
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        mAdapter = new MainAdapter(getContext());
        mRecyclerView.setAdapter(mAdapter);
        return view;
    }

    private CouponListLoadTask mCouponListTask;
    private boolean hasMoreCoupon = true;
    private boolean isLoadCouponSuccess = false;

    private void loadMoreCoupon() {
        if (hasMoreCoupon && mCouponListTask == null) {
            long lastId = -1;
            if (mAdapter != null) {
                lastId = mAdapter.getCouponLastId();
            }
            mCouponListTask = new CouponListLoadTask(getContext(), lastId, CouponConstant.COUPON_PARAM_LIMIT_DEFAULT);
            mCouponListTask.setResponseCallback(mCouponListCallback);
            mExecutor.execute(mCouponListTask);
        }
    }

    private CouponCommonCallback<CouponListResponseResult> mCouponListCallback = new CouponCommonCallback<CouponListResponseResult>() {

        @Override
        public void onLoadFinished(CouponListResponseResult result, int errorCode) {
            mCouponListTask = null;
            if (errorCode == CouponCommonCallback.NO_ERROR) {
                isLoadCouponSuccess = true;
                if (mAdapter != null) {
                    if (result != null && result.last_id > 0) {
                        if (result.list != null && result.list.length > 0) {
                            if (result.limit > result.list.length) {
                                hasMoreCoupon = false;
                            }
                            mAdapter.addMoreCouponList(result.list);
                        } else {
                            hasMoreCoupon = false;
                        }
                    } else {
                        if (result != null) {
                            if (result.list == null || result.list.length < result.limit) {
                                hasMoreCoupon = false;
                            }
                        }
                        mAdapter.setCouponAndCardList(cardList, result != null ? result.list : null);
                    }
                }
            }
        }
    };

    private CardCouponList cardList;

    private boolean hasInitData = false;
    private boolean isLoadCardSuccess = false;
    private boolean isCouponCheckingNew = false, isCheckedNew = false;

    @Override
    public void startLoadData() {
        if (!hasInitData) {
            initLoadData();
        } else {
            isLoadCouponSuccess = false;
            isLoadCardSuccess = false;
            if (!isCouponCheckingNew && isCheckedNew) {
                loadCard();
            } else if (!isCheckedNew) {
                checkNewCoupon();
            }
        }
    }
//    private void getLeleAccount(){
//        AccountInfoHepler.getInstance().createAccount(new AccountCommonCallback<String>() {
//            @Override
//            public void onSuccess(String result) {
//                AccountInfoHepler.getInstance().queryAccount(new AccountCommonCallback<AccountInfo>() {
//                    @Override
//                    public void onSuccess(AccountInfo result) {
//                    }
//
//                    @Override
//                    public void onError(int errorCode, String errorMsg) {
//                    }
//
//                    @Override
//                    public void onNoNet() {
//                    }
//                });
//            }
//
//            @Override
//            public void onError(int errorCode, String errorMsg) {
//            }
//
//            @Override
//            public void onNoNet() {
//            }
//        });
//    }

    private void initLoadData() {
        hasInitData = true;
        loadData();
        isCheckedNew = false;
        checkNewCoupon();
    }

    private MainServiceTask mServiceTask;
    private BannerTask mBannerTask;
    private MainTopTask mTopTask;
    private WalletServiceListBean mServiceListBean;
    private WalletBannerListBean mBannerListBean;

    private void loadData() {
        if (mServiceTask == null && mBannerTask == null) {
            showLoadingView();
        }
        if (mServiceTask == null) {
            mServiceTask = new MainServiceTask(getContext(), mServiceCallback);
            mExecutor.execute(mServiceTask);
        }
        if (mBannerTask == null) {
            mBannerTask = new BannerTask(getContext(), mBannerCallback, ActivityConstant.BUSINESS_ID.MAIN_ID);
            mExecutor.execute(mBannerTask);
        }
        if (mTopTask == null) {
            mTopTask = new MainTopTask(getContext(), mTopCallback);
            mExecutor.execute(mTopTask);
        }
    }

    private MainPanelHelper.Callback<WalletServiceListBean> mServiceCallback = new MainPanelHelper.Callback<WalletServiceListBean>() {

        @Override
        public void onLoadFromLocalFinished(WalletServiceListBean result, int errorCode) {
            if (result != null) {
                hideLoadingView();
                mServiceListBean = result;
                if (mAdapter != null) {
                    mAdapter.setServiceList(result.list);
                }
                downloadMeikemeiyinSilent(mServiceListBean.list);
            }
        }

        @Override
        public void onLoadFromNetworkFinished(WalletServiceListBean result, int errorCode, boolean needUpdate) {
            mServiceTask = null;
            //System.out.println("WalletFragment mServiceCallback onLoadFromNetworkFinished gotoType==" + gotoType);

            if (gotoType != -1 && result != null && result.list != null && result.list.length > 0) {
                for (final WalletServiceListBean.WalletServiceBean bean : result.list) {
                    if (bean.service_id == gotoType) {
                        Action.uploadExposeTab(Action.WALLET_HOME_LIST + bean.service_id);
                        if (bean.jump_type == WalletServiceListBean.WalletServiceBean.JUMP_TYPE_APP) {
                            Bundle bundle = null;
                            if (getContext() != null
                                    && !TextUtils.isEmpty(bean.package_name)
                                    && getContext().getPackageName().startsWith(bean.package_name)) {
                                bundle = new Bundle();
                                bundle.putString(WalletConstant.EXTRA_FROM, Action.EVENT_PROP_FROM_ICON);
                            }
                            AppUtils.LaunchAppWithBundle(getContext(), bean.package_name, bean.jump_param, bundle);
                        } else if (bean.jump_type == WalletServiceListBean.WalletServiceBean.JUMP_TYPE_WEB) {
                            if (AccountHelper.getInstance().isLogin(getContext())) {
                                jumpWeb(bean);
                            } else {
                                AccountHelper.getInstance().loginLetvAccountIfNot((Activity) getContext(), new AccountManagerCallback() {

                                    @Override
                                    public void run(AccountManagerFuture future) {
                                        try {
                                            if (getContext() != null && future.getResult() != null && AccountHelper.getInstance().isLogin(getContext())) {
                                                jumpWeb(bean);
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }
                        }
                    }
                }
            }
            if (!needUpdate) {
                return;
            }
            hideLoadingView();
            mServiceListBean = result;
            if (errorCode == MainPanelHelper.NO_ERROR) {
                if (mAdapter != null) {
                    mAdapter.setServiceList(result.list);
                }
                downloadMeikemeiyinSilent(mServiceListBean.list);
            } else if (errorCode == MainPanelHelper.ERROR_NETWORK) {
                showBlankPage(BlankPage.STATE_NETWORK_ABNORMAL).getIconView().setOnClickListener(mRetryClickListener);
            } else if (errorCode == MainPanelHelper.ERROR_NO_NETWORK) {
                showBlankPage(BlankPage.STATE_NO_NETWORK);
            }
        }
    };

    private void jumpWeb(WalletServiceListBean.WalletServiceBean bean) {
        if (bean == null) {
            return;
        }
        Intent intent = new Intent(getContext(), WalletMainWebActivity.class);
        intent.putExtra(CommonConstants.EXTRA_URL, bean.jump_link);
        intent.putExtra(CommonConstants.EXTRA_TITLE_NAME, bean.service_name);
        intent.putExtra(WalletConstant.EXTRA_WEB_WITH_ACCOUNT, bean.need_token == 1);
        getContext().startActivity(intent);
    }

    private MainPanelHelper.Callback<WalletBannerListBean> mBannerCallback = new MainPanelHelper.Callback<WalletBannerListBean>() {

        @Override
        public void onLoadFromLocalFinished(WalletBannerListBean result, int errorCode) {
            if (result != null) {
                mBannerListBean = result;
                if (mAdapter != null) {
                    mAdapter.setBannerList(result.list);
                }
            }
        }

        @Override
        public void onLoadFromNetworkFinished(WalletBannerListBean result, int errorCode, boolean needUpdate) {
            mBannerTask = null;
            if (!needUpdate) {
                return;
            }
            mBannerListBean = result;
            if (errorCode == MainPanelHelper.NO_ERROR) {
                if (mAdapter != null) {
                    mAdapter.setBannerList(result.list);
                }
            }
        }
    };

    private MainPanelHelper.Callback<WalletTopListBean> mTopCallback = new MainPanelHelper.Callback<WalletTopListBean>() {

        @Override
        public void onLoadFromLocalFinished(WalletTopListBean result, int errorCode) {
            if (result != null && null != result.list && errorCode == MainPanelHelper.NO_ERROR) {
                diaplayTopData(result.list);
            }
        }

        @Override
        public void onLoadFromNetworkFinished(WalletTopListBean result, int errorCode, boolean needUpdate) {
            mBannerTask = null;
            if (result != null && null != result.list && errorCode == MainPanelHelper.NO_ERROR) {
                diaplayTopData(result.list);
            }
        }
    };

    private void diaplayTopData(WalletTopListBean.WalletTopBean[] list) {
        if (null != list && list.length > 0) {
            if (topLayout.getVisibility() == View.GONE) {
                topLayout.setVisibility(View.VISIBLE);
            }
            if (list.length == 1) {
                oneButton.setDefaultData(list[0]);
            }
            if (list.length == 2) {
                oneButton.setDefaultData(list[0]);
                twoButton.setDefaultData(list[1]);
            }
            if (list.length == 3) {
                oneButton.setDefaultData(list[0]);
                twoButton.setDefaultData(list[1]);
                threeButton.setDefaultData(list[2]);
            }
        }

    }


    private View.OnClickListener mRetryClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (isNetworkAvailable()) {
                loadData();
                handleCardCoupnLoad();
            }
        }
    };
    private static final String SHAREPRE_DOWNLOAD_MEIKE = "hasDownloadMeiKeMeiYin";

    private void downloadMeikemeiyinSilent(WalletServiceListBean.WalletServiceBean[] serviceList) {
        if (serviceList == null || serviceList.length <= 0 || !NetworkHelper.isWifiAvailable()) {
            return;
        }
        boolean download = SharedPreferencesHelper.getBoolean(SHAREPRE_DOWNLOAD_MEIKE, false);
        if (!download) {
            for (WalletServiceListBean.WalletServiceBean bean : serviceList) {
                if (bean.jump_type != WalletServiceListBean.WalletServiceBean.JUMP_TYPE_APP) {
                    continue;
                }
                if (bean != null && "com.futurestar.mkmyle".equals(bean.package_name)
                        && !AppUtils.isAppInstalled(getContext(), bean.package_name)
                        && AppUtils.checkLetvAppStorePermission(getContext())) {
                    AppUtils.downloadAppSilent(getContext(), bean.package_name);
                    SharedPreferencesHelper.putBoolean(SHAREPRE_DOWNLOAD_MEIKE, true);
                    return;
                }
            }
        }
    }


    private void loadCard() {
        if (accountHelper.isLogin(getContext())) {
            if (mCardCouponTask == null) {
                mCardCouponTask = new CardCouponListTask(getContext(), mCardCouponCallback);
                mCardCouponTask.setParams(MovieOrder.MOVIE_TICKET_PROGRESS_UNCONSUMED, 5);
                mExecutor.execute(mCardCouponTask);
            }
        }
    }


    private CardCouponListTask mCardCouponTask;
    private CouponCommonCallback<CardCouponList> mCardCouponCallback = new CouponCommonCallback<CardCouponList>() {

        @Override
        public void onLoadFinished(CardCouponList result, int errorCode) {
            mCardCouponTask = null;
            if (errorCode == CouponCommonCallback.NO_ERROR) {
                isLoadCardSuccess = true;
                if (!isLoadCouponSuccess) {
                    cardList = result;
                    loadCoupon();  // 卡卷和优惠劵同时加载
                } else {
                    if (mAdapter != null) {
                        mAdapter.setCardCouponList(result);
                    }
                }
            }
        }
    };


    private void loadCoupon() {
        if (accountHelper.isLogin(getContext())) {
            if (mCouponListTask == null) {
                long lastId = -1;   //刷新请求 lastId应为-1
                /*if (mAdapter != null) {
                    lastId = mAdapter.getCouponLastId();
                }*/
                mCouponListTask = new CouponListLoadTask(getContext(), lastId, CouponConstant.COUPON_PARAM_LIMIT_DEFAULT);
                mCouponListTask.setResponseCallback(mCouponListCallback);
                mExecutor.execute(mCouponListTask);
            }
        }
    }


    private void checkNewCoupon() {
        if (accountHelper.isLogin(getContext()) && isNetworkAvailable()) {
            if (mCouponCheckNewTask == null) {
                isCouponCheckingNew = true;
                mCouponCheckNewTask = new CouponCheckNewTask(getContext(), mCouponCheckNewCallback);
                mExecutor.execute(mCouponCheckNewTask);
            }
        }
    }

    private CouponCheckNewTask mCouponCheckNewTask;
    private CouponCommonCallback mCouponCheckNewCallback = new CouponCommonCallback() {
        @Override
        public void onLoadFinished(Object result, int errorCode) {
            mCouponCheckNewTask = null;
            isCouponCheckingNew = false;
            isCheckedNew = true;
            loadCard();
        }
    };


    private void handleCardCoupnLoad() {
        if (isCheckedNew) { //登陆用户进入app时已领取优惠劵
            if (!isLoadCardSuccess || !isLoadCouponSuccess) {
                loadCard();
            }
        } else {
            checkNewCoupon();
        }
    }

    @Override
    public boolean hasBlankAndLoadingView() {
        return true;
    }


    @Override
    public void onNetWorkChanged(boolean isNetworkAvailable) {
        if (isNetworkAvailable) {
            if (mBannerListBean == null && mServiceListBean == null) {
                loadData();
            }
            handleCardCoupnLoad();
        }
    }

    @Override
    public boolean displayActionbar() {
        return true;
    }


    private int gotoType = -1;

    @Override
    public void gotoNext(int type) {
        gotoType = type;
    }
}
