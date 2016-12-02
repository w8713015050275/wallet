package com.letv.walletbiz.movie.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.letv.wallet.common.activity.BaseFragmentActivity;
import com.letv.walletbiz.R;
import com.letv.walletbiz.base.activity.BaseWalletFragmentActivity;
import com.letv.walletbiz.movie.MovieTicketConstant;
import com.letv.walletbiz.movie.beans.MovieDetail;
import com.letv.walletbiz.movie.fragment.MoviePhotoGalleryItemFragment;

import java.util.Arrays;

/**
 * Created by liuliang on 16-3-23.
 */
public class MoviePhotoGalleryActivity extends BaseWalletFragmentActivity implements View.OnClickListener {

    private MovieDetail.MovieAllSizePhoto[] mPhotoArray;
    private int mCurrentIndex;

    private View mToolbar;
    private ImageView mToolbarArrow;
    private TextView mToolbarTitle;

    private ViewPager mViewPager;
    private PhotoAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.movie_photo_gallery);

        Intent intent = getIntent();
        if (intent != null) {
            Parcelable[] parcelableArray = intent.getParcelableArrayExtra(MovieTicketConstant.EXTRA_MOVIE_PHOTO_ARRAY);
            if (parcelableArray != null) {
                mPhotoArray = Arrays.copyOf(parcelableArray, parcelableArray.length, MovieDetail.MovieAllSizePhoto[].class);
            }
            mCurrentIndex = intent.getIntExtra(MovieTicketConstant.EXTRA_MOVIE_PHOTO_INDEX, 0);
        }
        if (mPhotoArray == null || mPhotoArray.length <= 0) {
            finish();
            return;
        }
        if (mCurrentIndex < 0) {
            mCurrentIndex = 0;
        } else if (mCurrentIndex >= mPhotoArray.length) {
            mCurrentIndex = mPhotoArray.length - 1;
        }

        initView();
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
    public void onClick(View v) {
        if (v == mToolbarArrow) {
            finish();
        }
    }

    private void initView() {
        mToolbar = findViewById(R.id.toolbar);
        mToolbarArrow = (ImageView) findViewById(R.id.toolbar_arrow);
        mToolbarArrow.setOnClickListener(this);
        mToolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        mToolbarTitle.setText(getTitleStr());

        mViewPager = (ViewPager) findViewById(R.id.photo_viewpager);
        mAdapter = new PhotoAdapter(getSupportFragmentManager(), mPhotoArray);
        mViewPager.setAdapter(mAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mCurrentIndex = position;
                mToolbarTitle.setText(getTitleStr());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mViewPager.setCurrentItem(mCurrentIndex);
    }

    private String getTitleStr() {
        return (mCurrentIndex + 1) + "/" + mPhotoArray.length;
    }

    private void toggleToolBar() {
        if (mToolbar.getVisibility() == View.VISIBLE) {
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_out_top);
            animation.setAnimationListener(new Animation.AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mToolbar.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            mToolbar.startAnimation(animation);
        } else {
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_in_top);
            mToolbar.setVisibility(View.VISIBLE);
            mToolbar.startAnimation(animation);
        }
    }

    private class PhotoAdapter extends FragmentStatePagerAdapter implements View.OnClickListener {

        private MovieDetail.MovieAllSizePhoto[] mData;
        private SparseArray<MoviePhotoGalleryItemFragment> mFragments = new SparseArray<>();

        public PhotoAdapter(FragmentManager fm, MovieDetail.MovieAllSizePhoto[] data) {
            super(fm);
            mData = data;
        }

        @Override
        public int getItemPosition(Object object) {
            return PagerAdapter.POSITION_NONE;
        }

        @Override
        public Fragment getItem(int position) {
            MoviePhotoGalleryItemFragment fragment = mFragments.get(position);
            if (fragment == null) {
                fragment = new MoviePhotoGalleryItemFragment(position, mData[position].middle, this);
            }
            return fragment;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Object fragment = super.instantiateItem(container, position);
            mFragments.put(position, (MoviePhotoGalleryItemFragment) fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            mFragments.remove(position);
            super.destroyItem(container, position, object);
        }

        @Override
        public int getCount() {
            return mData == null ? 0 : mData.length;
        }

        @Override
        public void onClick(View v) {
            toggleToolBar();
        }
    }
}
