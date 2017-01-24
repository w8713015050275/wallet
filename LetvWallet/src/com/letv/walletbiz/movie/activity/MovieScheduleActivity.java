package com.letv.walletbiz.movie.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.DateUtils;
import com.letv.wallet.common.util.PermissionCheckHelper;
import com.letv.wallet.common.util.SharedPreferencesHelper;
import com.letv.wallet.common.view.BlankPage;
import com.letv.walletbiz.R;
import com.letv.walletbiz.base.activity.BaseWalletFragmentActivity;
import com.letv.walletbiz.base.util.Action;
import com.letv.walletbiz.base.util.WalletConstant;
import com.letv.walletbiz.movie.MovieTicketConstant;
import com.letv.walletbiz.movie.beans.CinemaSchedule;
import com.letv.walletbiz.movie.beans.CinemaSchedule.ScheduleList;
import com.letv.walletbiz.movie.beans.CinemaSchedule.ScheduleMovie;
import com.letv.walletbiz.movie.beans.Movie;
import com.letv.walletbiz.movie.fragment.CinemaListFragment;
import com.letv.walletbiz.movie.fragment.ScheduleFragment;
import com.letv.walletbiz.movie.ui.MovieGallery;
import com.letv.walletbiz.movie.ui.MovieGalleryImageView;
import com.letv.walletbiz.movie.ui.MovieTabFlowLayout;
import com.letv.walletbiz.movie.utils.BlurImageHelper;
import com.letv.walletbiz.movie.utils.CinemaListHelper;
import com.letv.walletbiz.movie.utils.MovieCommonCallback;
import com.letv.walletbiz.movie.utils.MoviePriorityExecutorHelper;
import com.letv.walletbiz.movie.utils.MovieScheduleFavoritesTask;
import com.letv.walletbiz.movie.utils.ScheduleTask;

import org.xutils.common.task.PriorityExecutor;
import org.xutils.xmain;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by liuliang on 16-1-26.
 */
public class MovieScheduleActivity extends BaseWalletFragmentActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener {

    private static final String SAVE_STATE_MOVIE_ID = "save_movie_id";
    private static final String SAVE_STATE_FAVORITE = "save_favorite";

    private String mCinemaName = null;
    private int mCinemaId = -1;
    private int mCityId = -1;
    private long mMovieId = -1;
    private String mDate;
    private boolean isFavorite;

    private TextView mCinemaAddressView;
    private View mTeleContainer;
    private ImageView mTeleView;
    private View mGalleryContainer;
    private MovieGallery mMovieGallery;
    private GalleryAdapter mMovieGalleryAdapter;

    private BlurImageHelper mBlurImageHelper;
    private int mGalleryCurrentPosition = -1;

    private TextView mMovieNameView;
    private TextView mMovieScoreView;
    private LinearLayout mCurrentMovieView;

    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private ViewpagerAdapter mPagerAdapter;

    private ScheduleTask mTask;
    private MovieScheduleFavoritesTask mFavoritesTask;

    private CinemaSchedule mCinemaSchedule;
    private ScheduleMovie[] mScheduleMovieArray;
    private ScheduleMovie mCurrentScheduleMovie;
    private AccountHelper accountHelper = AccountHelper.getInstance();
    private MenuItem menuItemFavorites;
    private PriorityExecutor mExecutor;
    public MovieTabFlowLayout tagContainer;
    private TextView mStopTimeView;

    private MovieCommonCallback<CinemaSchedule> mCallback = new MovieCommonCallback<CinemaSchedule>() {

        @Override
        public void onLoadFinished(CinemaSchedule result, int errorCode) {
            if(isFinishing()) return;
            hideLoadingView();
            if (errorCode == MovieCommonCallback.NO_ERROR) {
                mCinemaSchedule = result;
                mScheduleMovieArray = result.movie_list;
                if (TextUtils.isEmpty(mCinemaName)) {
                    mCinemaName = result.name;
                    setTitle(mCinemaName);
                }
                isFavorite = mCinemaSchedule.is_favorite == MovieTicketConstant.MOVIE_PARAM_FAVORITED ? true : false ;
                if (mScheduleMovieArray == null || mScheduleMovieArray.length == 0) {
                    BlankPage blankPage = showBlankPage();
                    blankPage.setCustomPage(getString(R.string.movie_error_cinema_no_movie), BlankPage.Icon.NO_ACCESS);
                } else {
                    mCinemaAddressView.setText(mCinemaSchedule.addr);
                    showSpecial(mCinemaSchedule.special_info_list);
                    if (mMovieGalleryAdapter != null) {
                        mMovieGalleryAdapter.setData(mScheduleMovieArray);
                    }
                    if (mMovieId > 0) {
                        if (mGalleryCurrentPosition == -1) {
                            mMovieGallery.setSelection(getMoviePosition(), true);
                        } else {
                            mMovieGallery.setSelection(mGalleryCurrentPosition, true);
                        }
                    } else {
                        mMovieGallery.setSelection((mScheduleMovieArray.length+1)/2-1, true);
                    }
                    if (TextUtils.isEmpty(mCinemaSchedule.tele)) {
                        mTeleContainer.setVisibility(View.GONE);
                    } else {
                        mTeleContainer.setVisibility(View.VISIBLE);
                    }
                    showStopTime(mCinemaSchedule.stop_time);
                }
            } else if (errorCode == MovieCommonCallback.ERROR_NETWORK) {
                showBlankPage(BlankPage.STATE_NETWORK_ABNORMAL).getIconView().setOnClickListener(mRetryClickListener);
            }
            mTask = null;
            showHideMenu();
        }
    };

    private Toast mTeleEmptyToast;

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tele_container:
                    if (PermissionCheckHelper.checkCallPermission(MovieScheduleActivity.this, 0) == PermissionCheckHelper.PERMISSION_ALLOWED) {
                        if (mCinemaSchedule == null) {
                            return;
                        }
                        if (TextUtils.isEmpty(mCinemaSchedule.tele)) {
                            if (mTeleEmptyToast == null) {
                                mTeleEmptyToast = Toast.makeText(MovieScheduleActivity.this, R.string.movie_phone_number_empty, Toast.LENGTH_LONG);
                            }
                            mTeleEmptyToast.show();
                            return;
                        }
                        Action.uploadCallout(Action.MOVIE_CALLOUT_CLICK);
                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + mCinemaSchedule.tele));
                        startActivity(intent);
                    }
                    break;
                case R.id.cinema_address:
                    if (mCinemaSchedule == null) {
                        break;
                    }
                    Action.uploadClick(Action.MOVIE_CINEMA_ADDRESS_CLICK);
                    try {
                        if (!TextUtils.isEmpty(mCinemaSchedule.addr)) {
                            Uri uri = Uri.parse("geo:" + mCinemaSchedule.latitude + "," + mCinemaSchedule.longitude + "?q=" + Uri.encode(mCinemaSchedule.addr));
                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                            startActivity(intent);
                            break;
                        }
                    } catch (Exception e) {

                    }
                    try {
                        Uri uri = Uri.parse("http://map.baidu.com/mobile/webapp/search/search/qt=s&searchFlag=bigBox&version=5&exptype=dep&c=undefined&wd=" + Uri.encode(mCinemaSchedule.addr));
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    } catch (Exception e1) {
                    }
                    break;
                case R.id.view_currentMovie:
                    gotoMovieDetail();
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
        handleIntent(getIntent());
        if (savedInstanceState != null) {
            long id = savedInstanceState.getLong(SAVE_STATE_MOVIE_ID, -1);
            isFavorite = savedInstanceState.getBoolean(SAVE_STATE_FAVORITE, isFavorite);
            if (id != -1) {
                mMovieId = id;
            }
        }
        registerNetWorkReceiver();

        setContentView(R.layout.movie_schedule_activity);

        mCinemaAddressView = (TextView) findViewById(R.id.cinema_address);
        mCinemaAddressView.setOnClickListener(mOnClickListener);
        mTeleContainer = findViewById(R.id.tele_container);
        mTeleView = (ImageView) findViewById(R.id.cinema_tele);
        mTeleContainer.setOnClickListener(mOnClickListener);
        tagContainer = (MovieTabFlowLayout) findViewById(R.id.cinema_tag_container);

        mGalleryContainer = findViewById(R.id.movie_gallery_container);
        mMovieGallery = (MovieGallery) findViewById(R.id.movie_galley);
        mMovieGallery.setOnItemClickListener(this);
        mMovieGallery.setOnItemSelectedListener(this);

        mMovieGalleryAdapter = new GalleryAdapter(this);
        mMovieGallery.setAdapter(mMovieGalleryAdapter);
        mMovieNameView = (TextView) findViewById(R.id.movie_name);
        mMovieScoreView = (TextView) findViewById(R.id.movie_score);
        mCurrentMovieView = (LinearLayout) findViewById(R.id.view_currentMovie);
        mCurrentMovieView.setOnClickListener(mOnClickListener);

        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mStopTimeView = (TextView) findViewById(R.id.tvStopTime);

        mPagerAdapter = new ViewpagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);
        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        mExecutor = MoviePriorityExecutorHelper.getPriorityExecutor();
        mBlurImageHelper = new BlurImageHelper(this);
    }

    @Override
    public boolean hasBlankAndLoadingView() {
        return true;
    }

    private void handleIntent(Intent intent){
        if (intent != null) {
            mGalleryCurrentPosition = -1 ;
            String from = null;
            Uri uri = intent.getData();
            if (uri != null) {
                mCinemaName = uri.getQueryParameter(MovieTicketConstant.EXTRA_CINEMA_NAME);
                String temp = uri.getQueryParameter(MovieTicketConstant.EXTRA_CINEMA_ID);
                try {
                    mCinemaId = Integer.parseInt(temp);
                } catch (NumberFormatException e) {
                }
                temp = uri.getQueryParameter(MovieTicketConstant.EXTRA_CITY_ID);
                try {
                    mCityId = Integer.parseInt(temp);
                } catch (NumberFormatException e) {
                    mCityId = -1;
                }
                temp = uri.getQueryParameter(MovieTicketConstant.EXTRA_MOVIE_ID);
                try {
                    mMovieId = Long.parseLong(temp);
                } catch (NumberFormatException e) {
                    mMovieId = -1;
                }
                mDate = uri.getQueryParameter(MovieTicketConstant.EXTRA_DATE);
                from = uri.getQueryParameter(WalletConstant.EXTRA_FROM);
            } else {
                mCinemaName = intent.getStringExtra(MovieTicketConstant.EXTRA_CINEMA_NAME);
                mCinemaId = intent.getIntExtra(MovieTicketConstant.EXTRA_CINEMA_ID, -1);
                mCityId = intent.getIntExtra(MovieTicketConstant.EXTRA_CITY_ID, -1);
                mMovieId = intent.getLongExtra(MovieTicketConstant.EXTRA_MOVIE_ID, -1);
                mDate = intent.getStringExtra(MovieTicketConstant.EXTRA_DATE);
                int favorites = intent.getIntExtra(MovieTicketConstant.MOVIE_PARAM_FAVORITES, MovieTicketConstant.MOVIE_PARAM_UNFAVORITED);
                isFavorite = favorites == MovieTicketConstant.MOVIE_PARAM_FAVORITED ? true : false ;
                from = intent.getStringExtra(WalletConstant.EXTRA_FROM);
            }
            Action.uploadCinemaDetailExpose(String.valueOf(mCinemaId), from);
            if (!TextUtils.isEmpty(mDate)) {
                char[] array = mDate.toCharArray();
                StringBuilder builder = new StringBuilder();
                for (int i=0; i<array.length; i++) {
                    if (array[i] != '-') {
                        builder.append(array[i]);
                    }
                }
                mDate = builder.toString();
            }
            if (mCityId == -1) {
                mCityId = SharedPreferencesHelper.getInt(MovieTicketConstant.PREFERENCES_CURRENT_CITY_ID, -1);
            }
        }
        setTitle(mCinemaName);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(getIntent());
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    @Override
    protected void onDestroy() {
        if (mBlurImageHelper != null) {
            mBlurImageHelper.clearMemCache();
            mBlurImageHelper.recycle();
        }
        super.onDestroy();
    }

    @Override
    protected void onNetWorkChanged(boolean isNetworkAvailable) {
        if (isNetworkAvailable) {
            loadData();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(SAVE_STATE_MOVIE_ID, mMovieId);
        outState.putBoolean(SAVE_STATE_FAVORITE, isFavorite);
    }

    private void loadData() {
        if (isNetworkAvailable()) {
            if (mTask == null) {
                showLoadingView();
                mTask = new ScheduleTask(mCallback);
                mTask.execute(mCityId, mCinemaId);
            }
        } else {
            showBlankPage(BlankPage.STATE_NO_NETWORK);
        }
    }

    // 影院标签最多显示6个, 且不支持超大字体
    private void showSpecial(CinemaSchedule.SpecialInfo[] specialInfos){
        if (specialInfos == null || specialInfos.length == 0) {
            tagContainer.setVisibility(View.GONE);
            return;
        } else {
            tagContainer.setVisibility(View.VISIBLE);
            tagContainer.removeAllViews();
            LayoutInflater inflater = LayoutInflater.from(this);
            TextView child;
            CinemaSchedule.SpecialInfo special = null;
            for ( int i = 0 ; i< specialInfos.length ; i++) {
                special = specialInfos[i];
                if(special == null || TextUtils.isEmpty(special.text)) continue;
                child = (TextView) inflater.inflate(R.layout.movie_special_item, tagContainer, false);
                child.setText(special.text);
                tagContainer.addView(child);
            }
        }
    }

    private void showStopTime(int stopTime){
        if(stopTime > 0){
            mStopTimeView.setText(String.format(getString(R.string.movie_schedule_stop_time_tip), stopTime));
        }else {
            mStopTimeView.setVisibility(View.GONE);
        }
    }

    private int getMoviePosition() {
        if (mScheduleMovieArray == null || mScheduleMovieArray.length <= 0 || mMovieId <= 0) {
            return 0;
        }
        ScheduleMovie movie;
        for (int i=0; i<mScheduleMovieArray.length; i++) {
            movie = mScheduleMovieArray[i];
            if (movie.id == mMovieId) {
                mGalleryCurrentPosition = i;
                return i;
            }
        }
        return 0;
    }

    private void gotoMovieDetail() {
        Intent intent = new Intent(MovieScheduleActivity.this, MovieDetailActivity.class);
        intent.putExtra(MovieTicketConstant.EXTRA_MOVIE_ID, mMovieId);
        if (mCurrentScheduleMovie != null) {
            intent.putExtra(MovieTicketConstant.EXTRA_MOVIE_NAME, mCurrentScheduleMovie.name);
        }
        if (mCityId != -1) {
            intent.putExtra(MovieTicketConstant.EXTRA_CITY_ID, mCityId);
        }
        intent.putExtra(WalletConstant.EXTRA_FROM, Action.EVENT_PROP_FROM_APP);
        startActivity(intent);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mMovieGalleryAdapter != null && mMovieGalleryAdapter.getItem(position) != null) {
            if (mMovieId == mMovieGalleryAdapter.getItem(position).id) {
                gotoMovieDetail();
            }
        }

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, final View view, final int position, long id) {
        final ScheduleMovie movie = mMovieGalleryAdapter.getItem(position);
        mGalleryCurrentPosition = position;
        mBlurImageHelper.getBlurImage(movie.poster_url, new BlurImageHelper.BlurCallback() {

            @Override
            public void onBlurFinished(String url , Drawable blurIcon) {
                if(url.equals(movie.poster_url)){
                    mGalleryContainer.setBackground(blurIcon);
                }
            }
        });
        if (movie != null) {
            mCurrentScheduleMovie = movie;
            mMovieId = movie.id;
            mMovieNameView.setText(movie.name);
            mMovieScoreView.setText(getScoreStr(movie));
            if (mPagerAdapter != null) {
                mPagerAdapter.setData(movie);
            }
            mTabLayout.setupWithViewPager(mViewPager);
            mViewPager.setCurrentItem(getCurrentItem(movie));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_cinema_schedule, menu);
        menuItemFavorites = menu.findItem(R.id.action_favorites);
        menuItemFavorites.setCheckable(true);
        showHideMenu();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_favorites) {
         handleFavoriteAction(!item.isChecked());
        }
        return super.onOptionsItemSelected(item);
    }

    private void handleFavoriteAction(final boolean addFavorite) {
        if (accountHelper.isLogin(this)) {
            if (mFavoritesTask == null) {
                mFavoritesTask = new MovieScheduleFavoritesTask(this, new MovieCommonCallback() {
                    @Override
                    public void onLoadFinished(Object result, int errorCode) {
                        if (errorCode == NO_ERROR && menuItemFavorites != null) {
                            isFavorite = addFavorite;
                            menuItemFavorites.setChecked(isFavorite);
                            int res = isFavorite ? R.drawable.menu_favorites_checked : R.drawable.menu_favorites_uncheck;
                            menuItemFavorites.setIcon(res);
                            updateCinemaList();
                        } else {
                            int resId = addFavorite ? R.string.movie_schedule_add_favorites_fail : R.string.movie_schedule_cancle_favorites_fail;
                            Toast.makeText(MovieScheduleActivity.this, resId, Toast.LENGTH_SHORT).show();
                        }
                        mFavoritesTask = null;
                    }
                });
                mFavoritesTask.setParams(mCinemaId, addFavorite);
                mExecutor.execute(mFavoritesTask);
            }
        } else {
            AccountHelper.getInstance().loginLetvAccountIfNot(this, null);
        }
    }

    private void updateCinemaList(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                CinemaListHelper.deleteCinemaListFromLocal(MovieScheduleActivity.this, mCityId, -1, null);
                sendBroadcast(new Intent(CinemaListFragment.ACTION_CINEMA_FAVORITES_CHANGE));
            }
        }).start();
    }

    private void showHideMenu() {
        if (menuItemFavorites != null && accountHelper != null) {
            if (accountHelper.isLogin(this)) {
                menuItemFavorites.setChecked(isFavorite);
                int res = isFavorite ? R.drawable.menu_favorites_checked : R.drawable.menu_favorites_uncheck;
                menuItemFavorites.setIcon(res);
            } else {
                menuItemFavorites.setChecked(false);
                menuItemFavorites.setIcon(R.drawable.menu_favorites_uncheck);
            }
        }
    }

    private int getCurrentItem(ScheduleMovie movie) {
        if (mDate != null && movie != null) {
            ScheduleList[] schedules = movie.sche;
            if (schedules == null) {
                return 0;
            }
            ScheduleList schedule;
            for (int i=0; i<schedules.length; i++) {
                schedule = schedules[i];
                if (mDate.equals(schedule.date)) {
                    return i;
                }
            }
        }
        mDate = null;
        return 0;
    }

    private CharSequence getScoreStr(Movie movie) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        if (movie.score > 0) {
            builder.append(String.valueOf(movie.score));
            builder.append(getString(R.string.movie_score), new AbsoluteSizeSpan(10, true), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        } else {
            builder.append(String.valueOf(movie.want_count));
            builder.append(getString(R.string.movie_want), new AbsoluteSizeSpan(10, true), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        return builder;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private class GalleryAdapter extends BaseAdapter {

        private Context mContext;
        private ScheduleMovie[] mMovieList = null;

        public GalleryAdapter(Context context) {
            mContext = context;
        }

        public void setData(ScheduleMovie[] data) {
            mMovieList = data;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mMovieList == null ? 0 : mMovieList.length;
        }

        @Override
        public ScheduleMovie getItem(int position) {
            return mMovieList == null ? null : mMovieList[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.movie_cinema_schedule_gallery_item, parent, false);
            }
            MovieGalleryImageView view = (MovieGalleryImageView) convertView;
            ScheduleMovie movie = getItem(position);
            xmain.image().bind(view, movie.poster_url);
            return view;
        }
    }

    class ViewpagerAdapter extends FragmentPagerAdapter {
        private HashMap<String, ScheduleFragment> mFragmentHashMap = new HashMap<String, ScheduleFragment>();
        private ScheduleList[] mScheduleArray;
        private ScheduleMovie mScheduleMovie;
        private FragmentManager mFragmentManager;

        public ViewpagerAdapter(FragmentManager fm) {
            super(fm);
            mFragmentManager = fm;
        }

        public void setData(ScheduleMovie movie) {
            if ( isFinishing()) return;
            FragmentTransaction transaction = mFragmentManager.beginTransaction();
            for (Fragment fragment : mFragmentHashMap.values()) {
                transaction.remove(fragment);
            }
            transaction.commitAllowingStateLoss();
            mFragmentHashMap.clear();
            mScheduleMovie = movie;
            mScheduleArray = mScheduleMovie.sche;
            notifyDataSetChanged();
        }

        @Override
        public Fragment getItem(int position) {
            ScheduleList list = mScheduleArray[position];
            ScheduleFragment fragment = mFragmentHashMap.get(list.date);
            if (fragment == null) {
                fragment = new ScheduleFragment();
                mFragmentHashMap.put(list.date, fragment);
            }
            Bundle bundle = new Bundle();
            bundle.putInt(MovieTicketConstant.EXTRA_MOVIE_LONGS, mCurrentScheduleMovie.getMovieLongs());
            bundle.putParcelableArray(MovieTicketConstant.EXTRA_MOVIE_SCHEDULE, list.detail);
            bundle.putParcelableArray(MovieTicketConstant.EXTRA_MOVIE_DISCOUNT_INFO, list.discount_info);
            bundle.putString(MovieTicketConstant.EXTRA_DATE, list.date);
            bundle.putString(MovieTicketConstant.EXTRA_MOVIE_NAME, mScheduleMovie.name);
            bundle.putInt(MovieTicketConstant.EXTRA_CINEMA_ID, mCinemaId);
            bundle.putLong(MovieTicketConstant.EXTRA_MOVIE_ID, mMovieId);
            bundle.putString(MovieTicketConstant.EXTRA_CINEMA_NAME, mCinemaName);
            fragment.updateArguments(bundle);
            return fragment;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            if (mScheduleArray != null && position >= 0 && position < mScheduleArray.length) {
                ScheduleList list = mScheduleArray[position];
                ScheduleFragment itemFragment = mFragmentHashMap.get(list.date);
                if (itemFragment == null) {
                    mFragmentHashMap.put(list.date, (ScheduleFragment) fragment);
                }
            }
            return fragment;
        }

        @Override
        public long getItemId(int position) {
            return mScheduleMovie != null ? (mScheduleMovie.id << 4  + position) : position;
        }

        @Override
        public int getCount() {
            return mScheduleArray == null ? 0 : mScheduleArray.length;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            ScheduleList list = mScheduleArray[position];
            return getDateFormatStr(list.date);
        }

        private String getDateFormatStr(String date) {
            Date dateObj = DateUtils.parseDate(date, "yyyyMMdd");
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
