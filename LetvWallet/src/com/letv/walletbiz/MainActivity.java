package com.letv.walletbiz;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TabHost;

import com.letv.shared.widget.LeBottomSheet;
import com.letv.shared.widget.LeTabWidget;
import com.letv.shared.widget.LeTabWidgetUtils;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.ExecutorHelper;
import com.letv.wallet.common.util.SharedPreferencesHelper;
import com.letv.wallet.common.widget.LeFragmentTabHost;
import com.letv.walletbiz.base.activity.BaseWalletFragmentActivity;
import com.letv.walletbiz.base.util.Action;
import com.letv.walletbiz.base.util.WalletConstant;
import com.letv.walletbiz.coupon.activity.CouponListActivity;
import com.letv.walletbiz.main.fragment.MainFragment;
import com.letv.walletbiz.main.fragment.RecommendFragment;
import com.letv.walletbiz.main.fragment.WalletFragment;
import com.letv.walletbiz.me.fragment.MeFragment;

import java.util.List;


public class MainActivity extends BaseWalletFragmentActivity implements TabHost.OnTabChangeListener {
    private static final String TAG = "MainActivity";

    public static final String WALLET_LICENCE_ACCEPT = "wallet_licence_accept";
    public static final int WALLET_LICENCE_REJECT = 0;
    public static final int WALLET_LICENCE_ACCEPT_ONCE = 1;
    public static final int WALLET_LICENCE_ACCEPT_FOREVER = 2;

    private int mCurrentTabId = 1;
    private LeFragmentTabHost mTabHost;
    private FrameLayout mRealTabCotent;
    public static final String TAG_RECOMMEND = "recommend";
    public static final String TAG_WALLET = "wallet";
    public static final String TAG_MY = "my";
    public static final int ID_RECOMMEND = 0;
    public static final int ID_WALLET = 1;
    public static final int ID_MY = 2;

    private FragmentManager fragmentManager;
    private AccountHelper accountHelper = AccountHelper.getInstance();
    private LeBottomSheet mLoginSheet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerNetWorkReceiver();
        setContentView(R.layout.activity_main);
        if (!accountHelper.isLogin(this)) {
            showLoginPrompt(this);
        } else {
            accountHelper.getTokenASync(this);
        }
        fragmentManager = getSupportFragmentManager();
        Action.uploadStartApp();
        findView();
        parseIntent(getIntent());
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


    public void updateAction(int type) {
        if (type == 0) {
            mUpdateHelper.requreyVersion();
        } else {
            mUpdateHelper.startUpgradeService();
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        parseIntent(intent);
    }


    private void parseIntent(Intent intent) {
        Uri uri = intent.getData();
        int tabId = 1;
        int serviceId = -1;
        if (uri != null) {
            String main_tab = uri.getQueryParameter("main_tab");
            String service_id = uri.getQueryParameter("service_id");
            try {
                tabId = Integer.parseInt(main_tab);
            } catch (NumberFormatException e) {
                tabId = 1;
            }
            try {
                serviceId = Integer.parseInt(service_id);
            } catch (NumberFormatException e) {
                serviceId = -1;
            }
        } else {
            tabId = intent.getIntExtra("main_tab", 1);
            serviceId = intent.getIntExtra("service_id", -1);
        }
        gotoTab(tabId);
        gotoService(serviceId);
        getIntent().removeExtra("main_tab");
        getIntent().removeExtra("service_id");

    }

    @Override
    protected void onStart() {
        super.onStart();
        changeActionbar();
    }

    @Override
    public boolean hasBlankAndLoadingView() {
        return true;
    }


    @Override
    protected void onDestroy() {
        Action.uploadStopApp();
        ExecutorHelper.getExecutor().clearAllRunnable();

        if (mLoginSheet != null && mLoginSheet.isShowing()) {
            mLoginSheet.dismiss();
        }
        super.onDestroy();
    }

    @Override
    protected void onNetWorkChanged(boolean isNetworkAvailable) {
    }


    private void findView() {

        mTabHost = (LeFragmentTabHost) findViewById(android.R.id.tabhost);
        mRealTabCotent = (FrameLayout) findViewById(R.id.main_fragment_container);
        mTabHost.setup(this, fragmentManager, R.id.main_fragment_container, mRealTabCotent);
        LeTabWidget tabWidget = (LeTabWidget) mTabHost.getTabWidget();
        tabWidget.setDividerDrawable(null);
        View indicatorView;
        LayoutInflater inflater = LayoutInflater.from(this);
        indicatorView = LeTabWidgetUtils.createIndicatorView(inflater, tabWidget, R.drawable.main_tab_recommend_bg,
                getString(R.string.main_tab_recommend));
        mTabHost.addTab(mTabHost.newTabSpec(TAG_RECOMMEND).setIndicator(indicatorView), RecommendFragment.class, null);

        indicatorView = LeTabWidgetUtils.createIndicatorView(inflater, tabWidget, R.drawable.main_tab_wallet_bg,
                getString(R.string.main_tab_wallet));
        mTabHost.addTab(mTabHost.newTabSpec(TAG_WALLET).setIndicator(indicatorView), WalletFragment.class, null);

        indicatorView = LeTabWidgetUtils.createIndicatorView(inflater, tabWidget, R.drawable.main_tab_my_bg,
                getString(R.string.main_tab_my));
        mTabHost.addTab(mTabHost.newTabSpec(TAG_MY).setIndicator(indicatorView), MeFragment.class, null);

        LeTabWidgetUtils.setTabWidgetLayout(this, tabWidget);
        tabWidget.setTitleTextColor(getColor(R.color.movie_ticket_tab_tv_color), getColor(R.color.movie_ticket_tab_tv_select_color));


        mTabHost.setOnTabChangedListener(this);
        mTabHost.setFragmentHiddenEnabled(true);
    }

    private void gotoTab(int tab) {
        if (mTabHost != null) {
            mTabHost.setCurrentTab(tab);
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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        permissionResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean canStartUpgradeService() {
        if (!isLicenceAccept()) {
            return false;
        }
        return true;
    }

    private boolean isLicenceAccept() {
        return SharedPreferencesHelper.getInt(WALLET_LICENCE_ACCEPT, WALLET_LICENCE_REJECT) == WALLET_LICENCE_ACCEPT_FOREVER;
    }

    @Override
    public void onTabChanged(String tabId) {
        mCurrentTabId = getTabIdByTag(tabId);
        setFragmentitle(mCurrentTabId);
    }

    private int getTabIdByTag(String s) {
        if (TAG_RECOMMEND.equals(s)) {
            return ID_RECOMMEND;
        } else if (TAG_WALLET.equals(s)) {
            return ID_WALLET;
        } else if (TAG_MY.equals(s)) {
            return ID_MY;
        }

        return 1;
    }

    private void setFragmentitle(int tabId) {
        int resId = -1;
        switch (tabId) {
            case ID_RECOMMEND:
                resId = R.string.main_tab_recommend;
                break;
            case ID_WALLET:
                resId = R.string.main_tab_wallet;
                break;
            case ID_MY:
                resId = R.string.main_tab_my;
                break;
        }
        if (resId > 0) {
            setTitle(resId);
        }
    }


    private MainFragment getCurrentFragment() {

        List<Fragment> list = fragmentManager.getFragments();
        if (list != null && !list.isEmpty()) {
            for (Fragment f : list) {
                if (getTabIdByTag(f.getTag()) == mCurrentTabId) {
                    return (MainFragment) f;
                }
            }
            return null;
        } else {
            return null;
        }
    }

    private void changeActionbar() {
        MainFragment main = getCurrentFragment();
        if (null != main) {
            main.changeActionbar();
        }
    }

    private void gotoService(int type) {
        MainFragment main = getCurrentFragment();
        if (null != main) {
            main.gotoNext(type);
        }
    }

    private void permissionResult(int requestCode,
                                  String permissions[], int[] grantResults) {
        MainFragment main = getCurrentFragment();
        if (null != main) {
            main.onRequestPermissionsResult(requestCode,
                    permissions, grantResults);
        }
    }

}
