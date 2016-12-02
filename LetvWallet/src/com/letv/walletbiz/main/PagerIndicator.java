package com.letv.walletbiz.main;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.letv.wallet.common.util.DensityUtils;
import com.letv.walletbiz.R;

/**
 * Created by liuliang on 16-4-14.
 */
public class PagerIndicator extends LinearLayout {

    private LayoutParams mLayoutParams;

    public PagerIndicator(Context context) {
        super(context);
        initLayoutParams();
    }

    public PagerIndicator(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initLayoutParams();
    }

    public PagerIndicator(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initLayoutParams();
    }

    private void initLayoutParams() {
        mLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        mLayoutParams.setMargins(0, 0, (int) DensityUtils.dip2px(2.5f), 0);
    }

    public void setTotalPageSize(int size) {
        if (size == 1) {
            removeAllViews();
            return;
        }
        if (size == getChildCount()) {
            return;
        }
        if (size > getChildCount()) {
            while (getChildCount() < size) {
                ImageView imageView = new ImageView(getContext());
                imageView.setImageResource(R.drawable.pager_indicator);
                addView(imageView, mLayoutParams);
            }
        } else {
            while (getChildCount() > size) {
                removeViewAt(getChildCount() - 1);
            }
        }
    }

    public int getPageCount() {
        return getChildCount();
    }

    public void setCurrentPage(int index) {
        if (getChildCount() <= 0) {
            return;
        }
        index %= getChildCount();
        for (int i=0; i<getChildCount(); i++) {
            View view = getChildAt(i);
            if (i == index) {
                view.setSelected(true);
            } else {
                view.setSelected(false);
            }
        }
    }
}
