package com.letv.walletbiz.movie.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.letv.shared.widget.LeLoadingView;
import com.letv.wallet.common.util.DateUtils;
import com.letv.wallet.common.util.DensityUtils;
import com.letv.wallet.common.util.ExecutorHelper;
import com.letv.wallet.common.util.SharedPreferencesHelper;
import com.letv.wallet.common.view.BlankPage;
import com.letv.walletbiz.R;
import com.letv.walletbiz.base.activity.BaseWalletFragmentActivity;
import com.letv.walletbiz.base.util.Action;
import com.letv.walletbiz.base.util.WalletConstant;
import com.letv.walletbiz.movie.MovieTicketConstant;
import com.letv.walletbiz.movie.beans.MovieDetail;
import com.letv.walletbiz.movie.fragment.MovieInformationFragment;
import com.letv.walletbiz.movie.fragment.MovieRecommendFragment;
import com.letv.walletbiz.movie.ui.MovieDetailPagerViewBehavior;
import com.letv.walletbiz.movie.utils.MovieCommonCallback;
import com.letv.walletbiz.movie.utils.MovieDetailTask;

import org.xutils.common.task.PriorityExecutor;
import org.xutils.image.ImageOptions;
import org.xutils.xmain;

import java.util.ArrayList;

/**
 * Created by liuliang on 16-3-17.
 */
public class MovieDetailActivity extends BaseWalletFragmentActivity implements MovieDetailPagerViewBehavior.StarPageHeaderViewScrollListener {

    private MovieDetailTask mMovieDetailTask;

    private CoordinatorLayout mCoordinatorLayout;
    private AppBarLayout mAppBarLayout;

    private ActionBar mActionBar;
    private TextView mToolBarTitle;
    private ImageView mToolBarBack;

    private ImageView mMoviePosterView;
    private TextView mMovieNameView;
    private TextView mMovieCategoryView;
    private TextView mMovieDurationView;
    private TextView mMovieDateView;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private ViewPagerAdpter mViewPagerAdapter;

    private View mBuyContainer;
    private TextView mBuyButton;

    private LeLoadingView mLoadingView;
    private BlankPage mBlankPage;

    private ArrayList<String> mTabNameArray = new ArrayList<>();

    private int mCityId = -1;
    private long mMovieId = -1;
    private String mMovieName;

    private MovieDetail mMovieDetail;

    private static final int MSG_MOVIE_DEATAIL_LOAD_SUCCEED = 1;
    private static final int MSG_NETWORK_CHANGED = 2;

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_MOVIE_DEATAIL_LOAD_SUCCEED:
                    hideDetailLoadingView();
                    int erroCode = msg.arg1;
                    if (erroCode == MovieCommonCallback.NO_ERROR) {
                        mMovieDetail = (MovieDetail) msg.obj;
                        if (mMovieDetail != null) {
                            updateView(mMovieDetail);
                            if (mViewPagerAdapter != null) {
                                mViewPagerAdapter.setData(mMovieDetail);
                            }
                        }
                    } else if (erroCode == MovieCommonCallback.ERROR_NETWORK) {
                        showDetailBlankPage();
                        mBlankPage.setPageState(BlankPage.STATE_NETWORK_ABNORMAL, mRetryClickListener);
                    }
                    break;
                case MSG_NETWORK_CHANGED:
                    if (mMovieDetail == null && mMovieDetailTask == null) {
                        loadData();
                    }
                    break;
            }
        }
    };

    private MovieCommonCallback<MovieDetail> mCallback = new MovieCommonCallback<MovieDetail>() {

        @Override
        public void onLoadFinished(MovieDetail result, int errorCode) {
            Message msg = mHandler.obtainMessage(MSG_MOVIE_DEATAIL_LOAD_SUCCEED);
            msg.arg1 = errorCode;
            msg.obj = result;
            msg.sendToTarget();
            mMovieDetailTask = null;
        }
    };

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.button_buy:
                    goToCinemaList();
                    break;
            }
        }
    };

    private View.OnClickListener mRetryClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            loadData();
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerNetWorkReceiver();
        setContentView(R.layout.movie_detail_layout);
        mTabNameArray.add(getString(R.string.movie_detail_tab_information));
        mTabNameArray.add(getString(R.string.movie_detail_tab_recommend));
        initActionBar();
        initView();
        handleIntent(getIntent(), true);
    }

    private void handleIntent(Intent intent, boolean needRefreshData){
        if (intent != null) {
            String from = null;
            Uri uri = intent.getData();
            if (uri != null) {
                String temp = uri.getQueryParameter(MovieTicketConstant.EXTRA_MOVIE_ID);
                try {
                    mMovieId = Long.parseLong(temp);
                } catch (NumberFormatException e) {
                }
                mMovieName = uri.getQueryParameter(MovieTicketConstant.EXTRA_MOVIE_NAME);
                temp = uri.getQueryParameter(MovieTicketConstant.EXTRA_CITY_ID);
                try {
                    mCityId = Integer.parseInt(temp);
                } catch (NumberFormatException e) {
                }
                from = uri.getQueryParameter(WalletConstant.EXTRA_FROM);
            } else {
                mMovieId = intent.getLongExtra(MovieTicketConstant.EXTRA_MOVIE_ID, -1);
                mMovieName = intent.getStringExtra(MovieTicketConstant.EXTRA_MOVIE_NAME);
                mCityId = intent.getIntExtra(MovieTicketConstant.EXTRA_CITY_ID, -1);
                from = intent.getStringExtra(WalletConstant.EXTRA_FROM);
            }
            Action.uploadMovieDetailExpose(String.valueOf(mMovieId), from);
        }
        if (mMovieId < 0) {
            finish();
        }
        if (mCityId == -1) {
            mCityId = SharedPreferencesHelper.getInt(MovieTicketConstant.PREFERENCES_CURRENT_CITY_ID, -1);
        }
        if(mToolBarTitle != null && !TextUtils.isEmpty(mMovieName)) {mToolBarTitle.setText(mMovieName);}
        if(needRefreshData) {
            mMovieDetailTask = null;
            loadData();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(getIntent(), true);
    }

    @Override
    public boolean hasBlankAndLoadingView() {
        return false;
    }

    @Override
    public boolean hasToolbar() {
        return false;
    }

    @Override
    protected void onNetWorkChanged(boolean isNetworkAvailable) {
        mHandler.sendEmptyMessage(MSG_NETWORK_CHANGED);
    }

    @Override
    public void onScrollPercent(float percent) {
        mMovieNameView.setAlpha(percent);
        mMovieCategoryView.setAlpha(percent);
        mMovieDurationView.setAlpha(percent);
        mMovieDateView.setAlpha(percent);
        mActionBar.getCustomView().setAlpha(1 - percent);
        mActionBar.getCustomView().setTranslationX(100 * percent);
        if (percent == 1.0f) {
            mCoordinatorLayout.requestLayout();
        }
    }

    @Override
    public void onScrolledShow() {

    }

    @Override
    public void onScrolledHide() {

    }

    private void initActionBar() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(false);
            mActionBar.setDisplayShowTitleEnabled(false);
            mActionBar.setCustomView(R.layout.movie_detail_tool_bar);
            mActionBar.setDisplayShowCustomEnabled(true);
            RelativeLayout customView = (RelativeLayout) mActionBar.getCustomView();
            customView.setTranslationX(100);
            mToolBarTitle = (TextView) customView.findViewById(R.id.toolbar_title);
            mToolBarBack = (ImageView) customView.findViewById(R.id.toolbar_arrow);
            mToolBarBack.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
    }

    private void initView() {
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.movie_detail_root);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        mMoviePosterView = (ImageView) findViewById(R.id.movie_poster);
        scalePosterBy16_9();
        mMovieNameView = (TextView) findViewById(R.id.movie_name);
        mMovieCategoryView = (TextView) findViewById(R.id.movie_category);
        mMovieDurationView = (TextView) findViewById(R.id.movie_duration);
        mMovieDateView = (TextView) findViewById(R.id.movie_date);
        mTabLayout = (TabLayout) findViewById(R.id.tab_indicator);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPagerAdapter = new ViewPagerAdpter(getSupportFragmentManager(), mTabNameArray);
        mViewPager.setAdapter(mViewPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 1) {
                    Action.uploadClick(Action.MOVIE_RECOMMENDED_PAGE_CLICK);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mTabLayout.setTabMode(TabLayout.MODE_FIXED);
        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        mTabLayout.setupWithViewPager(mViewPager);

        mBuyContainer = findViewById(R.id.buy_container);
        mBuyButton = (TextView) findViewById(R.id.button_buy);
        mBuyButton.setOnClickListener(mOnClickListener);

        mLoadingView = (LeLoadingView) findViewById(R.id.loading_view);
        mBlankPage = (BlankPage) findViewById(R.id.blank_page);

        CoordinatorLayout.Behavior behavior = ((CoordinatorLayout.LayoutParams) mViewPager.getLayoutParams()).getBehavior();
        MovieDetailPagerViewBehavior pageHeaderViewBehavior = (MovieDetailPagerViewBehavior) behavior;
        pageHeaderViewBehavior.setScrollListener(this);
    }

    private void scalePosterBy16_9(){
        if(mMoviePosterView != null){
            int width = DensityUtils.getScreenWidth();
            int height = width * 9 / 16 ;
            ViewGroup.LayoutParams params = mMoviePosterView.getLayoutParams();
            params.height = height;
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            mMoviePosterView.setLayoutParams(params);
        }
    }

    private void updateView(MovieDetail movieDetail) {
        if (movieDetail == null) {
            return;
        }
        MovieDetail.MoviePhoto moviePhoto = movieDetail.pictures;
        if (moviePhoto != null) {
            ImageOptions.Builder builder = new ImageOptions.Builder();
            builder.setConfig(Bitmap.Config.RGB_565);
            builder.setCrop(false);
            builder.setFailureDrawableId(R.drawable.place_holder_img).setLoadingDrawableId(R.drawable.place_holder_img);
            xmain.image().bind(mMoviePosterView, moviePhoto.backgroundImg, builder.build());
        }
        if(mToolBarTitle != null && TextUtils.isEmpty(mMovieName)) {
            mMovieName = movieDetail.name;
            mToolBarTitle.setText(mMovieName);
        }
        mMovieNameView.setText(movieDetail.name);
        if (!TextUtils.isEmpty(movieDetail.category)) {
            mMovieCategoryView.setText(movieDetail.category.replace('/', '、'));
        }
        if (TextUtils.isEmpty(movieDetail.longs)) {
            mMovieDurationView.setVisibility(View.GONE);
        } else {
            mMovieDurationView.setVisibility(View.VISIBLE);
            mMovieDurationView.setText(movieDetail.longs);
        }
        if (!TextUtils.isEmpty(movieDetail.relaseDate)) {
            mMovieDateView.setText(DateUtils.convertPatternForDate(movieDetail.relaseDate, "yyyy-MM-dd", getString(R.string.movie_detail_date_formatter)));
        }

        if (movieDetail.ticketBuy == 1) {
            mBuyContainer.setVisibility(View.VISIBLE);
            mViewPager.setPadding(0, 0, 0, (int) DensityUtils.dip2px(60));
        } else {
            mBuyContainer.setVisibility(View.GONE);
            mViewPager.setPadding(0, 0, 0, 0);
        }
    }

    private void showDetailLoadingView() {
        setCustomVisibility(View.GONE);
        mBlankPage.setVisibility(View.GONE);
        mLoadingView.setVisibility(View.VISIBLE);
        mLoadingView.appearAnim();
    }

    private void hideDetailLoadingView() {
        setCustomVisibility(View.VISIBLE);
        mBlankPage.setVisibility(View.GONE);
        mLoadingView.disappearAnim(null);
        mLoadingView.setVisibility(View.GONE);
    }

    private void showDetailBlankPage() {
        setCustomVisibility(View.GONE);
        mLoadingView.setVisibility(View.GONE);
        mBlankPage.setVisibility(View.VISIBLE);
        mBlankPage.bringToFront();
    }

    private void hideDetailBlankPage() {
        setCustomVisibility(View.VISIBLE);
        mLoadingView.setVisibility(View.GONE);
        mBlankPage.setVisibility(View.GONE);
    }

    private void setCustomVisibility(int visibility) {
        mAppBarLayout.setVisibility(visibility);
        mViewPager.setVisibility(visibility);
        mBuyContainer.setVisibility(visibility);
    }

    private void loadData() {
        if (mMovieDetailTask == null) {
            if (!isNetworkAvailable()) {
                showDetailBlankPage();
                mBlankPage.setPageState(BlankPage.STATE_NO_NETWORK, null);
            } else {
                showDetailLoadingView();
                mMovieDetailTask = new MovieDetailTask(this, mMovieId, mCityId, mCallback);
                ExecutorHelper.getExecutor().runnableExecutor(mMovieDetailTask);
            }
        }
    }

    private void goToCinemaList() {
        if (mMovieDetail == null) {
            return;
        }
        Action.uploadBuy(Action.MOVIE_PAY_CLICK, String.valueOf(mMovieDetail.movie_id));
        Intent intent = new Intent(this, CinemaListByMovieActivity.class);
        intent.putExtra(MovieTicketConstant.EXTRA_MOVIE_NAME, mMovieDetail.name);
        intent.putExtra(MovieTicketConstant.EXTRA_MOVIE_ID, mMovieDetail.movie_id);
        intent.putExtra(MovieTicketConstant.EXTRA_SCHE_DATE, mMovieDetail.sche_date);
        intent.putExtra(MovieTicketConstant.EXTRA_CITY_ID, mCityId);
        startActivity(intent);
    }

    private class ViewPagerAdpter extends FragmentPagerAdapter {

        private MovieDetail mMovieDetail;

        private ArrayList<String> mTabName;

        private MovieInformationFragment mMovieInformationFragment;
        private MovieRecommendFragment mMovieRecommendFragment;

        public ViewPagerAdpter(FragmentManager fm, ArrayList<String> tabName) {
            super(fm);
            mTabName = tabName;
        }

        public void setData(MovieDetail movieDetail) {
            mMovieDetail = movieDetail;
            if (mMovieDetail == null) {
                return;
            }
            if (mMovieInformationFragment != null) {
                mMovieInformationFragment.setData(movieDetail);
            }
            if (mMovieRecommendFragment != null) {
                if (mMovieDetail.workTag != null && mMovieDetail.workTag.length > 0) {
                    mMovieRecommendFragment.setTagId(mMovieDetail.workTag[0].tag_id);
                }
            }
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            if (position == 0) {
                if (mMovieInformationFragment == null) {
                    mMovieInformationFragment = new MovieInformationFragment();
                }
                fragment = mMovieInformationFragment;
            } else if (position == 1) {
                if (mMovieRecommendFragment == null) {
                    mMovieRecommendFragment = new MovieRecommendFragment();
                }
                fragment = mMovieRecommendFragment;
            }

            return fragment;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            if (position == 0) {
                if (mMovieInformationFragment == null && fragment instanceof MovieInformationFragment) {
                    mMovieInformationFragment = (MovieInformationFragment) fragment;
                }
            } else if (position == 1) {
                if (mMovieRecommendFragment == null && fragment instanceof MovieRecommendFragment) {
                    mMovieRecommendFragment = (MovieRecommendFragment) fragment;
                }
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return mTabName.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTabName.get(position);
        }
    }
}
