package com.letv.walletbiz.main.fragment;

import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.letv.tracker2.enums.EventType;
import com.letv.tracker2.enums.Key;
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
import com.letv.walletbiz.base.widget.MainTopLayout;
import com.letv.walletbiz.coupon.CouponConstant;
import com.letv.walletbiz.coupon.activity.CouponDetailActivity;
import com.letv.walletbiz.coupon.beans.BaseCoupon;
import com.letv.walletbiz.coupon.beans.CardCouponList;
import com.letv.walletbiz.coupon.beans.CouponListResponseResult;
import com.letv.walletbiz.coupon.utils.CardCouponListTask;
import com.letv.walletbiz.coupon.utils.CouponCheckNewTask;
import com.letv.walletbiz.coupon.utils.CouponCommonCallback;
import com.letv.walletbiz.coupon.utils.CouponListLoadTask;
import com.letv.walletbiz.main.AutoSlideViewpager;
import com.letv.walletbiz.main.BannerTask;
import com.letv.walletbiz.main.MainAdapter;
import com.letv.walletbiz.main.MainPanelHelper;
import com.letv.walletbiz.main.MainServiceTask;
import com.letv.walletbiz.main.MainTopTask;
import com.letv.walletbiz.main.WalletMainWebActivity;
import com.letv.walletbiz.main.bean.WalletBannerListBean;
import com.letv.walletbiz.main.bean.WalletServiceListBean;
import com.letv.walletbiz.movie.MovieTicketConstant;
import com.letv.walletbiz.movie.activity.MovieOrderDetailActivity;
import com.letv.walletbiz.movie.beans.MovieOrder;

import org.xutils.common.task.PriorityExecutor;

import java.util.HashMap;
import java.util.Map;

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

    private MainTopLayout topLayout;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parseIntent(getActivity().getIntent());
    }

    private int parseIntent(Intent intent) {
        Uri uri = intent.getData();
        int serviceId = -1;
        if (uri != null) {
            String service_id = uri.getQueryParameter("service_id");
            try {
                serviceId = Integer.parseInt(service_id);
            } catch (NumberFormatException e) {
            }
        } else {
            serviceId = intent.getIntExtra("service_id", -1);
        }
        gotoType = serviceId;
        bundle=intent.getExtras();
        return gotoType;
    }

    @Override
    public View onCreateCustomView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_tab_wallet, null);

        topLayout = (MainTopLayout) view.findViewById(R.id.wallet_top_layout);


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
                //添加数据埋点
                if (newState == RecyclerView.SCROLL_STATE_IDLE && mAdapter != null) {
                    //判断是否有卡券数据
                    if (mAdapter.getItemCount() > mAdapter.HEADER_COUNT) {
                        //获取此次滑动的初始位置和最终停止的位置
                        int first = mLayoutManager.findFirstVisibleItemPosition();
                        int last = mLayoutManager.findLastVisibleItemPosition();

                        for (int i = first; i <= last; i++) {
                            int type=mAdapter.getItemViewType(i);
                            if(type==MainAdapter.VIEW_TYPE_CARD_COUPON){
                                MovieOrder order= (MovieOrder)mAdapter.getItem(i);
                                if(!order.upData){
                                    //数据埋点，并且把数据是否已经添加数据埋点置为true，防止多次上传
                                    Action.uploadExpose(Action.WALLET_HOME_COUPON_EXPOSE,mAdapter.getDataPosition(i)+1,"");
                                    order.upData=true;
                                }
                            }else if(type==MainAdapter.VIEW_TYPE_COUPON){
                                //数据埋点，并且把数据是否已经添加数据埋点置为true，防止多次上传
                                BaseCoupon coupon= (BaseCoupon)mAdapter.getItem(i);
                                if(!coupon.upData){
                                    //数据埋点
                                    Action.uploadExpose(Action.WALLET_HOME_COUPON_EXPOSE,mAdapter.getDataPosition(i)+1,"");
                                    coupon.upData=true;
                                }
                            }else{
                                continue;
                            }
                        }
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
        mRecyclerView.setVisibility(View.VISIBLE);
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
        topLayout.loadButtonData();
    }


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
            //可能会直接跳转到子服务，取决于activity的传递参数
            gotoNext(gotoType,bundle);

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
            topLayout.loadButtonData();
        }
    }

    private int gotoType = -1;

    @Override
    public void gotoNext(int gotoType,Bundle bundle) {
        if (gotoType != -1 && mServiceListBean != null && mServiceListBean.list != null && mServiceListBean.list.length > 0) {
            for (final WalletServiceListBean.WalletServiceBean bean : mServiceListBean.list) {
                if (bean.service_id == gotoType) {
                    Action.uploadExposeTab(Action.WALLET_HOME_LIST + bean.service_id);
                    if (bean.jump_type == WalletServiceListBean.WalletServiceBean.JUMP_TYPE_APP) {
                        AppUtils.LaunchAppWithBundle(getContext(), bean.package_name, bean.jump_param, bundle, true);
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
            gotoType = -1;
        }
    }

    @Override
    public void fragmentDisplay() {
        String from = "";
        Map<String, Object> props = new HashMap<>();
        Intent intent = null;
        if (getActivity() != null) {
            intent = getActivity().getIntent();
            from = intent.getStringExtra(WalletConstant.EXTRA_FROM);
            props.put(Key.From.getKeyId(), from);
        }
        Action.uploadCustom(EventType.Expose, Action.WALLET_MAIN_EXPOSE, props);
        if (intent != null) {
            String NULL = null;
            intent.putExtra(WalletConstant.EXTRA_FROM, NULL);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            Intent intent = new Intent();
            intent.setAction(AutoSlideViewpager.SLIDE_PAUSE_LISTENER);
            getContext().sendBroadcast(intent);
        }else {
            Intent intent = new Intent();
            intent.setAction(AutoSlideViewpager.SLIDE_START_LISTENER);
            getContext().sendBroadcast(intent);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Intent intent = new Intent();
        intent.setAction(AutoSlideViewpager.SLIDE_PAUSE_LISTENER);
        getContext().sendBroadcast(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        Intent intent = new Intent();
        intent.setAction(AutoSlideViewpager.SLIDE_START_LISTENER);
        getContext().sendBroadcast(intent);
    }
}
