package com.letv.walletbiz.movie.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;

import com.letv.wallet.common.activity.BaseFragmentActivity;
import com.letv.wallet.common.util.DateUtils;
import com.letv.wallet.common.util.PermissionCheckHelper;
import com.letv.walletbiz.R;
import com.letv.walletbiz.base.activity.BaseWalletFragmentActivity;
import com.letv.walletbiz.movie.MovieTicketConstant;
import com.letv.walletbiz.movie.fragment.CinemaListFragment;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by liuliang on 16-1-23.
 */
public class CinemaListByMovieActivity extends BaseWalletFragmentActivity {

    private ViewPager mViewPager;
    private TabLayout mTabIndicator;

    private String mMoviename;
    private String[] mScheDate = null;
    private long mMovieId = -1;
    private int mCityId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_cinema_list_by_movie);

        Intent intent = getIntent();
        if (intent != null) {
            Uri uri = intent.getData();
            if (uri != null) {
                mMoviename = uri.getQueryParameter(MovieTicketConstant.EXTRA_MOVIE_NAME);
                String scheDateStr = uri.getQueryParameter(MovieTicketConstant.EXTRA_SCHE_DATE);
                if (!TextUtils.isEmpty(scheDateStr)) {
                    mScheDate = scheDateStr.split(",");
                }
                String movieIdStr = uri.getQueryParameter(MovieTicketConstant.EXTRA_MOVIE_ID);
                try {
                    mMovieId = Long.parseLong(movieIdStr);
                } catch (NumberFormatException e) {
                }
                String cityIdStr = uri.getQueryParameter(MovieTicketConstant.EXTRA_CITY_ID);
                try {
                    mCityId = Integer.parseInt(cityIdStr);
                } catch (NumberFormatException e) {
                }
            } else {
                mMoviename = intent.getStringExtra(MovieTicketConstant.EXTRA_MOVIE_NAME);
                mScheDate = intent.getStringArrayExtra(MovieTicketConstant.EXTRA_SCHE_DATE);
                mMovieId = intent.getLongExtra(MovieTicketConstant.EXTRA_MOVIE_ID, -1);
                mCityId = intent.getIntExtra(MovieTicketConstant.EXTRA_CITY_ID, -1);
            }
        }

        if (mScheDate == null || mMovieId == -1) {
            finish();
            return;
        }

        setTitle(mMoviename);

        PermissionCheckHelper.checkLocationPermission(this, 0);

        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mTabIndicator = (TabLayout) findViewById(R.id.pager_indicator);

        ViewPagerAdapter mAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);

        mTabIndicator.setTabGravity(TabLayout.GRAVITY_FILL);
        mTabIndicator.setupWithViewPager(mViewPager);

    }

    @Override
    public boolean hasBlankAndLoadingView() {
        return false;
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private HashMap<String, CinemaListFragment> mFragmentMap = new HashMap<String, CinemaListFragment>();

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            CinemaListFragment fragment = mFragmentMap.get(mScheDate[position]);
            if (fragment == null) {
                fragment = new CinemaListFragment();
                Bundle bundle = new Bundle();
                bundle.putString(CinemaListFragment.EXTRA_DATE, DateUtils.convertPatternForDate(mScheDate[position], "yyyy-MM-dd", "yyyyMMdd"));
                bundle.putLong(CinemaListFragment.EXTRA_MOVIE_ID, mMovieId);
                bundle.putInt(MovieTicketConstant.EXTRA_CITY_ID, mCityId);
                fragment.setArguments(bundle);
                fragment.setLocationPermissionRequested(true);
                mFragmentMap.put(mScheDate[position], fragment);
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return mScheDate.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getDateFormatStr(mScheDate[position]);
        }

        private String getDateFormatStr(String date) {
            Date dateObj = DateUtils.parseDate(date, "yyyy-MM-dd");
            String result = null;
            if (DateUtils.isToday(dateObj)) {
                result = DateUtils.formatDate(dateObj, getString(R.string.movie_cinema_list_by_movie_today));
            } else if (DateUtils.isTomorrow(dateObj)) {
                result = DateUtils.formatDate(dateObj, getString(R.string.movie_cinema_list_by_movie_tomorrow));
            } else {
                result = DateUtils.formatDate(dateObj, getString(R.string.movie_cinema_list_by_movie_normal));
            }
            return result;
        }
    }
}
