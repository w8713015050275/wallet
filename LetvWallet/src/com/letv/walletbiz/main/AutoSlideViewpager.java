package com.letv.walletbiz.main;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by liuliang on 16-5-26.
 */
public class AutoSlideViewpager extends ViewPager {

    private PagerIndicator mPagerIndicator;

    private boolean isChanging = false;

    private int mCurrentPosition = 0;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private Runnable mUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            if (getAdapter() != null) {
                int count = getAdapter().getCount();
                mCurrentPosition ++;
                if (mCurrentPosition >= count) {
                    mCurrentPosition = 0;
                }
                setCurrentItem(mCurrentPosition);
            }
        }
    };

    public AutoSlideViewpager(Context context) {
        this(context, null);
    }

    public AutoSlideViewpager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void dataSetChanged() {
        PagerAdapter pagerAdapter = getAdapter();
        if (pagerAdapter != null && pagerAdapter.getCount() > 1) {
            isChanging = true;
            mCurrentPosition = 0;
            setCurrentItem(mCurrentPosition);
            mHandler.removeCallbacks(mUpdateRunnable);
            mHandler.postDelayed(mUpdateRunnable, 3000);
        } else {
            isChanging = false;
        }
    }

    public void setPagerIndicator(PagerIndicator pagerIndicator) {
        mPagerIndicator = pagerIndicator;
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (isChanging) {
            if (View.VISIBLE == visibility) {
                mHandler.postDelayed(mUpdateRunnable, 3000);
            } else {
                mHandler.removeCallbacks(mUpdateRunnable);
            }
        }
    }

    private void init() {
        this.addOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (mPagerIndicator != null) {
                    mPagerIndicator.setCurrentPage(position);
                }
                mCurrentPosition = position;
                if (getAdapter() != null && getAdapter().getCount() > 1) {
                    mHandler.removeCallbacks(mUpdateRunnable);
                    mHandler.postDelayed(mUpdateRunnable, 3000);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }
}
