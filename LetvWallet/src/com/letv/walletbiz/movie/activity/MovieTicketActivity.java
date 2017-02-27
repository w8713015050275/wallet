package com.letv.walletbiz.movie.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;

import com.letv.shared.widget.LeTabWidget;
import com.letv.shared.widget.LeTabWidgetUtils;
import com.letv.wallet.common.util.SharedPreferencesHelper;
import com.letv.wallet.common.widget.LeFragmentTabHost;
import com.letv.walletbiz.R;
import com.letv.walletbiz.base.activity.BaseWalletFragmentActivity;
import com.letv.walletbiz.base.util.Action;
import com.letv.walletbiz.base.util.WalletConstant;
import com.letv.walletbiz.movie.MovieTicketConstant;
import com.letv.walletbiz.movie.fragment.CinemaListFragment;
import com.letv.walletbiz.movie.fragment.MovieHomeFragment;
import com.letv.walletbiz.movie.fragment.MovieOrderListFragment;

/**
 * Created by liuliang on 15-12-29.
 */
public class MovieTicketActivity extends BaseWalletFragmentActivity implements TabHost.OnTabChangeListener {

    public static final String TAG_MOVIE = "movie";
    public static final String TAG_CINEMA = "cinema";
    public static final String TAG_ORDER = "order";

    public static final int ID_MOVIE = 0;
    public static final int ID_CINEMA = 1;
    public static final int ID_ORDER = 2;

    private LeFragmentTabHost mTabHost;
    private FrameLayout mRealTabCotent;

    private int mCurrentTabId = -1;
    private ActionBar mActionBar;
    private RelativeLayout customView;
    private TextView mToolBarTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_ticket_main);

        parseIntent(getIntent());

        int cityId = SharedPreferencesHelper.getInt(MovieTicketConstant.PREFERENCES_CURRENT_CITY_ID, -1);
        if (cityId == -1) {
            Intent intent = new Intent(this, CityListActivity.class);
            intent.putExtra("first", true);
            if (mCurrentTabId != -1) {
                intent.putExtra(MovieTicketConstant.EXTRA_MOVIE_TICKET_TAB_ID, mCurrentTabId);
            }
            startActivity(intent);
            finish();
            return;
        }

        initView();
    }

    @Override
    public boolean hasBlankAndLoadingView() {
        return false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        parseIntent(intent);
        if (mCurrentTabId != -1 && mTabHost != null) {
            mTabHost.setCurrentTab(mCurrentTabId);
        }
    }

    private void parseIntent(Intent intent) {
        if (intent != null) {
            int tabId = -1;
            String from = null;
            Uri uri = intent.getData();
            if (uri != null) {
                String value = uri.getQueryParameter(MovieTicketConstant.EXTRA_MOVIE_TICKET_TAB_ID);
                try {
                    tabId = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                }
                from = uri.getQueryParameter(WalletConstant.EXTRA_FROM);
                if (TextUtils.isEmpty(from)) {
                    from = intent.getStringExtra(WalletConstant.EXTRA_FROM);
                }
            } else {
                tabId = intent.getIntExtra(MovieTicketConstant.EXTRA_MOVIE_TICKET_TAB_ID, -1);
                from = intent.getStringExtra(WalletConstant.EXTRA_FROM);
            }
            Action.uploadMovieExpose(from);
            if (tabId != -1) {
                mCurrentTabId = tabId;
            }
        }
    }

    private void initView() {
        mActionBar = getSupportActionBar();
        initActionBar();
        mTabHost = (LeFragmentTabHost) findViewById(android.R.id.tabhost);
        mRealTabCotent = (FrameLayout) findViewById(R.id.realtabcontent);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent, mRealTabCotent);
        LeTabWidget tabWidget = (LeTabWidget) mTabHost.getTabWidget();
        tabWidget.setDividerDrawable(null);

        View indicatorView;
        LayoutInflater inflater = LayoutInflater.from(this);
        indicatorView = LeTabWidgetUtils.createIndicatorView(inflater, tabWidget, R.drawable.ic_movie_tab_movie,
                getString(R.string.movie_tab_movie));
        mTabHost.addTab(mTabHost.newTabSpec(TAG_MOVIE).setIndicator(indicatorView), MovieHomeFragment.class, null);

        indicatorView = LeTabWidgetUtils.createIndicatorView(inflater, tabWidget, R.drawable.ic_movie_tab_cinema,
                getString(R.string.movie_tab_cinema));
        mTabHost.addTab(mTabHost.newTabSpec(TAG_CINEMA).setIndicator(indicatorView), CinemaListFragment.class, null);

        indicatorView = LeTabWidgetUtils.createIndicatorView(inflater, tabWidget, R.drawable.ic_movie_tab_order,
                getString(R.string.movie_tab_order));
        mTabHost.addTab(mTabHost.newTabSpec(TAG_ORDER).setIndicator(indicatorView), MovieOrderListFragment.class, null);

        LeTabWidgetUtils.setTabWidgetLayout(this, tabWidget);
        tabWidget.setTitleTextColor(getColor(R.color.movie_ticket_tab_tv_color), getColor(R.color.movie_ticket_tab_tv_select_color));

        mTabHost.setOnTabChangedListener(this);
        mTabHost.setFragmentHiddenEnabled(true);

        if (mCurrentTabId == -1) {
            mCurrentTabId = ID_MOVIE;
        }
        mTabHost.setCurrentTab(mCurrentTabId);
        setFragmentitle(mCurrentTabId);
    }

    @Override
    public void onTabChanged(String tabId) {
        mCurrentTabId = getTabIdByTag(tabId);
        setFragmentitle(mCurrentTabId);
    }

    private int getTabIdByTag(String s) {
        if (TAG_MOVIE.equals(s)) {
            return ID_MOVIE;
        } else if (TAG_CINEMA.equals(s)) {
            return ID_CINEMA;
        } else if (TAG_ORDER.equals(s)) {
            return ID_ORDER;
        }

        return -1;
    }

    private void setFragmentitle(int tabId) {
        int resId = -1;
        boolean enableAction = false;
        switch (tabId) {
            case ID_MOVIE:
                resId = R.string.movie_tab_movie;
                enableAction = true;
                break;
            case ID_CINEMA:
                Action.uploadClick(Action.MOVIE_CINEMA_CLICK);
                resId = R.string.movie_tab_cinema;
                enableAction = true;
                break;
            case ID_ORDER:
                Action.uploadClick(Action.MOVIE_ORDER_CLICK);
                resId = R.string.movie_tab_order;
                enableAction = false;
                break;
        }
        if (resId > 0) {
            setTitle(resId);
        }
        if (mActionBar != null) {
            mActionBar.setDisplayShowCustomEnabled(enableAction);
        }
    }

    public void showCurrentCity(String mCurrentCityName) {
        if (mToolBarTitle != null) {
            mToolBarTitle.setText(mCurrentCityName);
        }
    }

    private void initActionBar() {
        if (mActionBar != null) {
            mActionBar.setCustomView(R.layout.movie_ticket_tool_bar);
            mActionBar.setDisplayShowCustomEnabled(true);
            customView = (RelativeLayout) mActionBar.getCustomView();
            mToolBarTitle = (TextView) customView.findViewById(R.id.toolbar_title);
            mToolBarTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MovieTicketActivity.this, CityListActivity.class));
                }
            });
        }
    }

}
