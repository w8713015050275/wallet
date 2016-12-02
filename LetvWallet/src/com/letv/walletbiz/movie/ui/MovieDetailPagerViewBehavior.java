package com.letv.walletbiz.movie.ui;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by liuliang on 16-3-18.
 */
public class MovieDetailPagerViewBehavior extends AppBarLayout.ScrollingViewBehavior {
    private StarPageHeaderViewScrollListener scrollListener;
    private int startChildTop;

    public MovieDetailPagerViewBehavior() {
    }

    public MovieDetailPagerViewBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }



    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        boolean onDependentViewChanged = super.onDependentViewChanged(parent, child, dependency);

        AppBarLayout barLayout = (AppBarLayout) dependency;
        int totalScrollRange = barLayout.getTotalScrollRange();
        int barLayoutHeight = barLayout.getHeight();
        int total = barLayoutHeight - totalScrollRange;
        int cur = child.getTop() - total;
        float percent = (cur * 1f) / totalScrollRange;

        if(scrollListener != null){
            scrollListener.onScrollPercent(percent);
        }

        return onDependentViewChanged;
    }


    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, View child, View directTargetChild, View target, int nestedScrollAxes) {
        startChildTop = child.getTop();
        return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }


    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, View child, View target, int dx, int dy, int[] consumed) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed);
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, View child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
    }

    @Override
    public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, View child, View target) {
        super.onStopNestedScroll(coordinatorLayout, child, target);
        if (scrollListener == null) {
            return;
        }

        int dy = startChildTop - child.getTop();
        if (Math.abs(dy) < 100) {
            return;
        }
        if (dy > 0) {
            scrollListener.onScrolledHide();
        } else {
            scrollListener.onScrolledShow();
        }
    }

    public StarPageHeaderViewScrollListener getScrollListener() {
        return scrollListener;
    }

    public void setScrollListener(StarPageHeaderViewScrollListener scrollListener) {
        this.scrollListener = scrollListener;
    }


    public interface StarPageHeaderViewScrollListener {
        void onScrollPercent(float percent);

        void onScrolledShow();

        void onScrolledHide();
    }
}
