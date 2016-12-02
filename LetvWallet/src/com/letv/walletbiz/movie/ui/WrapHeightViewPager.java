package com.letv.walletbiz.movie.ui;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by liuliang on 16-1-30.
 */
public class WrapHeightViewPager extends ViewPager implements NestedScrollingParent, NestedScrollingChild {

    private int mSelectedPosition = 0;

    private OnPageChangeListener mOnPageChangeListener = new OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            mSelectedPosition = position;
            requestLayout();
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    public WrapHeightViewPager(Context context) {
        this(context, null);
    }

    public WrapHeightViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        addOnPageChangeListener(mOnPageChangeListener);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeOnPageChangeListener(mOnPageChangeListener);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        PagerAdapter adapter = getAdapter();
        if (adapter == null || !(adapter instanceof FragmentPagerAdapter)) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        FragmentPagerAdapter pagerAdapter = (FragmentPagerAdapter) adapter;
        if (mSelectedPosition < 0 || mSelectedPosition >= pagerAdapter.getCount()) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        Fragment fragment = pagerAdapter.getItem(mSelectedPosition);
        if (fragment != null) {
            View view = fragment.getView();
            if (view != null) {
                view.measure(widthMeasureSpec, MeasureSpec.UNSPECIFIED);
                setMeasuredDimension(widthMeasureSpec, view.getMeasuredHeight());
                return;
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
