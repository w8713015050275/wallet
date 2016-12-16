package com.letv.walletbiz;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PermissionInfo;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.letv.shared.widget.LeBottomSheet;
import com.letv.shared.widget.LeLicenceDialog;
import com.letv.shared.widget.LeNeverPermissionRequestDialog;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.AppUtils;
import com.letv.wallet.common.util.NetworkHelper;
import com.letv.wallet.common.util.SharedPreferencesHelper;
import com.letv.wallet.common.view.BlankPage;
import com.letv.walletbiz.base.activity.ActivityConstant;
import com.letv.walletbiz.base.activity.BaseWalletFragmentActivity;
import com.letv.walletbiz.base.util.Action;
import com.letv.walletbiz.base.util.WalletConstant;
import com.letv.walletbiz.coupon.CouponConstant;
import com.letv.walletbiz.coupon.activity.CouponDetailActivity;
import com.letv.walletbiz.coupon.activity.CouponListActivity;
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
import com.letv.walletbiz.main.bean.WalletBannerListBean;
import com.letv.walletbiz.main.bean.WalletServiceListBean;
import com.letv.walletbiz.movie.MovieTicketConstant;
import com.letv.walletbiz.movie.activity.MovieOrderDetailActivity;
import com.letv.walletbiz.movie.beans.MovieOrder;
import com.letv.walletbiz.order.activity.TotalOrderListActivity;
import com.letv.walletbiz.update.util.UpdateUtil;

import org.xutils.common.task.PriorityExecutor;

import java.util.ArrayList;
import java.util.List;

import timehop.stickyheader.RecyclerItemClickListener;


public class MainActivity extends BaseWalletFragmentActivity {
    private static final String TAG = "MainActivity";
    private LeBottomSheet mLoginSheet;
    private AccountHelper accountHelper = AccountHelper.getInstance();

    private static final String SHAREPRE_DOWNLOAD_MEIKE = "hasDownloadMeiKeMeiYin";

    private LeLicenceDialog mLeLicenceDialog;
    private LeNeverPermissionRequestDialog mLeNeverPermissionRequestDialog = null;
    private static final String WALLET_LICENCE_ACCEPT = "wallet_licence_accept";
    public static final int WALLET_LICENCE_REJECT = 0;
    public static final int WALLET_LICENCE_ACCEPT_ONCE = 1;
    public static final int WALLET_LICENCE_ACCEPT_FOREVER = 2;

    private static final int CHECK_PERMISSIONS = 100;
    private static final int PERMISSIONS_REQUEST_CODE = 1;

    private boolean hasInitData = false;
    private boolean isRequestingPermissin = false;

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private MainAdapter mAdapter;

    private WalletServiceListBean mServiceListBean;
    private WalletBannerListBean mBannerListBean;

    private PriorityExecutor mExecutor = new PriorityExecutor(3);
    private MainServiceTask mServiceTask;
    private BannerTask mBannerTask;
    private boolean isLoadCouponSuccess = false, isLoadCardSuccess = false, isGoSetting = false;
    private boolean isCouponCheckingNew = false, isCheckedNew = false;
    private CardCouponList cardList;


    private View.OnClickListener mRetryClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (isNetworkAvailable()) {
                loadData();
                handleCardCoupnLoad();
            }
        }
    };

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

    private CouponListLoadTask mCouponListTask;
    private boolean hasMoreCoupon = true;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerNetWorkReceiver();
        setContentView(R.layout.activity_main);
        if (!accountHelper.isLogin(this)) {
            showLoginPrompt(this);
        } else {
            accountHelper.getTokenASync(MainActivity.this);
        }

        initView();

        Action.uploadStartApp();
    }

    @Override
    protected void onStart() {
        super.onStart();
        boolean isLicenceAccept = isLicenceAccept();
        if (!isLicenceAccept) {
            showLeLicenceDialog();
        } else {
            if (hasPermission()) {
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
            } else if (!isRequestingPermissin) {
                checkMainPermission(PERMISSIONS_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        List<PermissionInfo> leNeverPermissions = new ArrayList<PermissionInfo>();
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE: {
                isRequestingPermissin = false;
                if (grantResults.length > 0) {
                    PackageManager packageManager = getPackageManager();
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            try {
                                PermissionInfo info = packageManager.getPermissionInfo(permissions[i], 0);
                                leNeverPermissions.add(info);
                            } catch (NameNotFoundException e) {
                            }
                        }
                    }
                }
                if (leNeverPermissions.size() <= 0) {
                    UpdateUtil.mIsStartedNewly = true;
                    mUpdateHelper.requreyVersion();
                    initLoadData();
                } else {
                    showLeNeverPermissionRequestDialog(leNeverPermissions);
                }
                break;
            }
        }
    }

    private boolean hasPermission() {
        String[] permissionList = new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        boolean result = true;
        for (String permission : permissionList) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                result = false;
            }
        }
        return result;
    }

    public boolean checkMainPermission(int requestCode) {
        boolean result = true;
        String[] permissionList = new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        ArrayList<String> deniedPermissionList = new ArrayList<String>();
        for (String permission : permissionList) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                result = false;
                deniedPermissionList.add(permission);
            }
        }
        if (deniedPermissionList.size() > 0) {
            isRequestingPermissin = true;
            ActivityCompat.requestPermissions(this, deniedPermissionList.toArray(new String[0]), requestCode);
        }
        return result;
    }

    private void showLeNeverPermissionRequestDialog(List<PermissionInfo> leNeverPermissions) {
        if (mLeNeverPermissionRequestDialog == null) {
            try {
                mLeNeverPermissionRequestDialog = new LeNeverPermissionRequestDialog(this, leNeverPermissions, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mLeNeverPermissionRequestDialog.disappear();
                        mLeNeverPermissionRequestDialog = null;
                        finish();
                    }
                });
                mLeNeverPermissionRequestDialog.setCancelable(false);
            } catch (Exception e) {
            }
        }
        mLeNeverPermissionRequestDialog.appear();
    }

    @Override
    public boolean hasBlankAndLoadingView() {
        return true;
    }

    @Override
    protected void onDestroy() {
        Action.uploadStopApp();
        if (mLeLicenceDialog != null) {
            mLeLicenceDialog.dismiss();
        }

        if (mLoginSheet != null && mLoginSheet.isShowing()) {
            mLoginSheet.dismiss();
        }

        super.onDestroy();
    }

    @Override
    protected void onNetWorkChanged(boolean isNetworkAvailable) {
        if (isNetworkAvailable) {
            if (mBannerListBean == null && mServiceListBean == null) {
                loadData();
            }
            handleCardCoupnLoad();
        }
    }

    private void initView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {

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
                    Intent intent = new Intent(MainActivity.this, MovieOrderDetailActivity.class);
                    intent.putExtra(MovieTicketConstant.EXTRA_MOVIE_ORDER_NUM, order.order_no);
                    startActivity(intent);
                } else if (item instanceof BaseCoupon) {
                    BaseCoupon coupon = (BaseCoupon) item;
                    Intent intent = new Intent(MainActivity.this, CouponDetailActivity.class);
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
        mAdapter = new MainAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void initLoadData() {
        hasInitData = true;
        loadData();
        isCheckedNew = false;
        checkNewCoupon();
    }

    private void loadData() {
        if (mServiceTask == null && mBannerTask == null) {
            showLoadingView();
        }
        if (mServiceTask == null) {
            mServiceTask = new MainServiceTask(this, mServiceCallback);
            mExecutor.execute(mServiceTask);
        }
        if (mBannerTask == null) {
            mBannerTask = new BannerTask(this, mBannerCallback, ActivityConstant.BUSINESS_ID.MAIN_ID);
            mExecutor.execute(mBannerTask);
        }
    }

    private void loadCard() {
        if (accountHelper.isLogin(this)) {
            if (mCardCouponTask == null) {
                mCardCouponTask = new CardCouponListTask(this, mCardCouponCallback);
                mCardCouponTask.setParams(MovieOrder.MOVIE_TICKET_PROGRESS_UNCONSUMED, 5);
                mExecutor.execute(mCardCouponTask);
            }
        }
    }

    private void loadCoupon() {
        if (accountHelper.isLogin(this)) {
            if (mCouponListTask == null) {
                long lastId = -1;   //刷新请求 lastId应为-1
                /*if (mAdapter != null) {
                    lastId = mAdapter.getCouponLastId();
                }*/
                mCouponListTask = new CouponListLoadTask(this, lastId, CouponConstant.COUPON_PARAM_LIMIT_DEFAULT);
                mCouponListTask.setResponseCallback(mCouponListCallback);
                mExecutor.execute(mCouponListTask);
            }
        }
    }

    private void checkNewCoupon() {
        if (accountHelper.isLogin(this) && isNetworkAvailable()) {
            if (mCouponCheckNewTask == null) {
                isCouponCheckingNew = true;
                mCouponCheckNewTask = new CouponCheckNewTask(this, mCouponCheckNewCallback);
                mExecutor.execute(mCouponCheckNewTask);
            }
        }
    }

    private void loadMoreCoupon() {
        if (hasMoreCoupon && mCouponListTask == null) {
            long lastId = -1;
            if (mAdapter != null) {
                lastId = mAdapter.getCouponLastId();
            }
            mCouponListTask = new CouponListLoadTask(this, lastId, CouponConstant.COUPON_PARAM_LIMIT_DEFAULT);
            mCouponListTask.setResponseCallback(mCouponListCallback);
            mExecutor.execute(mCouponListTask);
        }
    }

    private void handleCardCoupnLoad(){
        if (isCheckedNew) { //登陆用户进入app时已领取优惠劵
            if (!isLoadCardSuccess || !isLoadCouponSuccess) {
                loadCard();
            }
        } else {
            checkNewCoupon();
        }
    }

    private boolean isLicenceAccept() {
        return SharedPreferencesHelper.getInt(WALLET_LICENCE_ACCEPT, WALLET_LICENCE_REJECT) == WALLET_LICENCE_ACCEPT_FOREVER;
    }

    private void showLeLicenceDialog() {
        if (isLicenceAccept()) {
            return;
        }
        if (mLeLicenceDialog != null) {
            mLeLicenceDialog.dismiss();
        }
        mLeLicenceDialog = new LeLicenceDialog(this, getString(R.string.app_name),
                LeLicenceDialog.TYPE_USER_PRIVACY_CONTACTS_LOCATION_NET)
                .setLeLicenceDialogClickListener(new LeLicenceDialog.LeLicenceDialogClickListener() {

                    @Override
                    public void onClickListener(LeLicenceDialog.KEY key) {
                        switch (key) {
                            case BTN_AGREE:
                                acceptLicense();
                                mUpdateHelper.startUpgradeService();
                                break;
                            case BTN_CANCEL:
                                rejectLicense();
                                break;
                            case OUTSIDE:
                                finish();
                                break;
                        }
                    }
                }).show();
    }

    private void acceptLicense() {
        if (mLeLicenceDialog != null) {
            SharedPreferencesHelper.putInt(WALLET_LICENCE_ACCEPT,
                    mLeLicenceDialog.isChecked() ? WALLET_LICENCE_ACCEPT_FOREVER : WALLET_LICENCE_ACCEPT_ONCE);
            mLeLicenceDialog.dismiss();
            if (!hasPermission()) {
                checkMainPermission(PERMISSIONS_REQUEST_CODE);
            } else {
                initLoadData();
            }
        }
    }

    private void rejectLicense() {
        SharedPreferencesHelper.remove(WALLET_LICENCE_ACCEPT);
        finish();
    }

    private void showLoginPrompt(Context context) {
        mLoginSheet = new LeBottomSheet(context);
        String contextString = context.getResources().getString(
                R.string.wallet_prompt_login_title);
        mLoginSheet.setStyle(
                LeBottomSheet.BUTTON_DEFAULT_STYLE,
                loginListener,
                cancelListener,
                null,
                new String[]{
                        context.getString(R.string.wallet_prompt_login_toLogin),
                        context.getString(R.string.wallet_prompt_login_cancel)
                }, null,
                contextString, null,
                context.getResources().getColor(R.color.colorBtnBlue),
                false);
        mLoginSheet.show();
    }

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
                        && !AppUtils.isAppInstalled(this, bean.package_name)
                        && AppUtils.checkLetvAppStorePermission(this)) {
                    AppUtils.downloadAppSilent(this, bean.package_name);
                    SharedPreferencesHelper.putBoolean(SHAREPRE_DOWNLOAD_MEIKE, true);
                    return;
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_bills) {
            goToTotalOrderList();
        } else if (item.getItemId() == R.id.action_coupon) {
            Action.uploadExposeTab(Action.WALLET_HOME_COUPON);
            Intent intent = new Intent(this, CouponListActivity.class);
            intent.putExtra(WalletConstant.EXTRA_FROM, Action.EVENT_PROP_FROM_ICON);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void goToTotalOrderList() {
        Action.uploadExposeTab(Action.WALLET_HOME_TOTALORDER);
        Intent intent = new Intent(this, TotalOrderListActivity.class);
        startActivity(intent);
    }

    private View.OnClickListener loginListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            mLoginSheet.dismiss();
            AccountHelper accountHelper = AccountHelper.getInstance();
            accountHelper.addAccount(MainActivity.this, null);
        }
    };

    private View.OnClickListener cancelListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            mLoginSheet.dismiss();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean canStartUpgradeService() {
        if (!isLicenceAccept()) {
            return false;
        }
        return true;
    }
}
