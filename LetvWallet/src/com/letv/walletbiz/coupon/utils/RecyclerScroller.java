package com.letv.walletbiz.coupon.utils;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Created by lijunying on 16-4-20.
 */
public  class RecyclerScroller extends RecyclerView.OnScrollListener {
    private LinearLayoutManager mLinearLayoutManager;
    private boolean isloading = true;
    private int visibleThreshold = 5;
    private int previousTotal = 0;
    private int firstVisibleItem, visibleItemCount, totalItemCount;
    private  int lastVisibleItem;
    private boolean enableLoadMore = true;

    public RecyclerScroller(LinearLayoutManager mLinearLayoutManager, OnScrollListener listener) {
        this.mLinearLayoutManager = mLinearLayoutManager;
         this.listener = listener;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        if (enableLoadMore) {
            if (isloading) {
                if (totalItemCount > previousTotal) {
                    isloading = false;
                    previousTotal = totalItemCount;
                }
            }
            if (dy >= 0) {
                lastVisibleItem = mLinearLayoutManager.findLastVisibleItemPosition();
                visibleItemCount = recyclerView.getChildCount();
                totalItemCount = mLinearLayoutManager.getItemCount();
                if (!isloading && lastVisibleItem >= totalItemCount - visibleThreshold) {
                    // End has been reached
                    // Do something
                    if (listener != null) {
                        isloading = true;
                        listener.onLoadMore();
                    }

                }
            }
        }

    }

    public boolean isEnableLoadMore() {
        return enableLoadMore;
    }

    public void setEnableLoadMore(boolean enableLoadMore) {
        this.enableLoadMore = enableLoadMore;
    }

    private OnScrollListener listener;

    public interface OnScrollListener{
        void onLoadMore();
    }


}
