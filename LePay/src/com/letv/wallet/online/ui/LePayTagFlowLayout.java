package com.letv.wallet.online.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.letv.wallet.R;

/**
 * Created by changjiajie on 17-1-19.
 */
public class LePayTagFlowLayout extends ViewGroup {
    private boolean isSingleLine = false;

    public LePayTagFlowLayout(Context context) {
        super(context);
    }

    public LePayTagFlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TabFlowLayout);
        try {
            isSingleLine = a.getBoolean(R.styleable.TabFlowLayout_singleLine, false);
        } finally {
            a.recycle();
        }
    }

    public LePayTagFlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measureWidth = MeasureSpec.getSize(widthMeasureSpec);
        int measureHeight = MeasureSpec.getSize(heightMeasureSpec);
        int measureWidthMode = MeasureSpec.getMode(widthMeasureSpec);
        int measureHeightMode = MeasureSpec.getMode(heightMeasureSpec);

        int lineWidth = 0;
        int lineHeight = 0;
        int height = 0;
        int width = 0;
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            MarginLayoutParams lp = null;
            if (child.getLayoutParams() instanceof MarginLayoutParams) {
                lp = (MarginLayoutParams) child.getLayoutParams();
            } else {
                lp = new MarginLayoutParams(0, 0);
            }
            int childWidth = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            int childHeight = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;

            if (lineWidth + childWidth > measureWidth) {
                if (!isSingleLine) {
                    width = Math.max(lineWidth, width);
                    height += lineHeight;
                    lineHeight = childHeight;
                    lineWidth = childWidth;
                }
            } else {
                lineHeight = Math.max(lineHeight, childHeight);
                lineWidth += childWidth;
            }

            if (i == count - 1) {
                height += lineHeight + getPaddingBottom() + getPaddingTop();
                width = Math.max(width + getPaddingLeft() + getPaddingRight(), lineWidth);
            }

        }
        setMeasuredDimension((measureWidthMode == MeasureSpec.EXACTLY) ? measureWidth : width, (measureHeightMode == MeasureSpec.EXACTLY) ? measureHeight : height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();
        int lineWidth = 0;
        int lineHeight = 0;
        int top = 0, left = getPaddingLeft();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            int childWidth = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            int childHeight = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;

            if (childWidth + lineWidth > getMeasuredWidth()) {
                if (!isSingleLine) {
                    top += lineHeight;
                    left = getPaddingLeft();
                    lineHeight = childHeight;
                    lineWidth = childWidth;
                } else {
                    return;
                }
            } else {
                lineHeight = Math.max(lineHeight, childHeight);
                lineWidth += childWidth;
            }

            int lc = left + lp.leftMargin;
            int tc = top + lp.topMargin;
            int rc = lc + child.getMeasuredWidth();
            int bc = tc + child.getMeasuredHeight();
            child.layout(lc, tc, rc, bc);
            left += childWidth;
        }

    }
}