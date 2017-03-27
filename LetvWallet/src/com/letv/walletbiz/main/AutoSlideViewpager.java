package com.letv.walletbiz.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
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
            if (!mEnableAutoSlide) {
                return;
            }
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
    private Boolean mEnableAutoSlide = true;

    public AutoSlideViewpager(Context context) {
        this(context, null);
    }

    public AutoSlideViewpager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        IntentFilter filter_dynamic = new IntentFilter();
        filter_dynamic.addAction(SLIDE_PAUSE_LISTENER);
        filter_dynamic.addAction(SLIDE_START_LISTENER);
        context.registerReceiver(dynamicReceiver,filter_dynamic);
    }

    public void enableAutoSlide() {
        mEnableAutoSlide = true;
        mHandler.removeCallbacks(mUpdateRunnable);
        mHandler.postDelayed(mUpdateRunnable, 3000);
    }

    public void disableAutoSlide() {
        mEnableAutoSlide = false;
        mHandler.removeCallbacks(mUpdateRunnable);
    }

    public void dataSetChanged() {
        AutoSlidePagerAdapter pagerAdapter = (AutoSlidePagerAdapter) getAdapter();
        if (pagerAdapter != null && pagerAdapter.getRealCount() > 1) {
            isChanging = true;
            mCurrentPosition = pagerAdapter.getRealCount();
            setCurrentItem(mCurrentPosition, false);
            if (mEnableAutoSlide) {
                mHandler.removeCallbacks(mUpdateRunnable);
                mHandler.postDelayed(mUpdateRunnable, 3000);
            }
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
                if (mEnableAutoSlide) {
                    mHandler.postDelayed(mUpdateRunnable, 3000);
                }
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
                AutoSlidePagerAdapter adapter = (AutoSlidePagerAdapter) getAdapter();
                if (adapter != null && adapter.getCount() > 1) {
                    if (mEnableAutoSlide) {
                        mHandler.removeCallbacks(mUpdateRunnable);
                        mHandler.postDelayed(mUpdateRunnable, 3000);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    AutoSlidePagerAdapter adapter = (AutoSlidePagerAdapter) getAdapter();
                    if (adapter == null || adapter.getRealCount() <= 1) {
                        return;
                    }
                    int newPos;
                    if (mCurrentPosition < adapter.getRealCount()) {
                        newPos = mCurrentPosition + adapter.getRealCount();
                        setCurrentItem(newPos, false);
                        mCurrentPosition = newPos;
                    } else if(mCurrentPosition >= adapter.getRealCount() * 2){
                        newPos = adapter.getItemIndexForPosition(mCurrentPosition);
                        setCurrentItem(newPos, false);
                        mCurrentPosition = newPos;
                    }
                }
            }
        });

    }

    //解决有时出现空白页面的问题
    public static final String SLIDE_PAUSE_LISTENER="slide_pause_listener";
    public static final String SLIDE_START_LISTENER="slide_start_listener";
    private BroadcastReceiver dynamicReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(SLIDE_PAUSE_LISTENER)){
                disableAutoSlide();
            }else if(intent.getAction().equals(SLIDE_START_LISTENER)){
                enableAutoSlide();
            }
        }
    };
}
