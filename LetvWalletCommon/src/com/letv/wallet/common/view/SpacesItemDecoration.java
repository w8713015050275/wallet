package com.letv.wallet.common.view;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by changjiajie on 16-12-13.
 */

public class SpacesItemDecoration extends RecyclerView.ItemDecoration {

    private int mLeftSpace;
    private int mTopSpace;
    private int mRightSpace;
    private int mBottomSpace;

    public SpacesItemDecoration(int space) {
        mLeftSpace = mTopSpace = mRightSpace = mBottomSpace = space;
    }

    public SpacesItemDecoration(int left, int top, int right, int bottom) {
        mLeftSpace = left;
        mTopSpace = top;
        mRightSpace = right;
        mBottomSpace = bottom;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view,
                               RecyclerView parent, RecyclerView.State state) {
        outRect.left = mLeftSpace;
        outRect.top = mTopSpace;
        outRect.right = mRightSpace;
        outRect.bottom = mBottomSpace;
    }
}
