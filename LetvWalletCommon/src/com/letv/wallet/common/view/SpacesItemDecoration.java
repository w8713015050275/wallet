package com.letv.wallet.common.view;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by changjiajie on 16-12-13.
 */

public class SpacesItemDecoration extends RecyclerView.ItemDecoration {


    private int mSpace = 0;

    public SpacesItemDecoration(int space) {
        this.mSpace = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view,
                               RecyclerView parent, RecyclerView.State state) {
        outRect.top = mSpace;
        outRect.left = mSpace;
        outRect.right = mSpace;
        outRect.bottom = mSpace;
    }
}
