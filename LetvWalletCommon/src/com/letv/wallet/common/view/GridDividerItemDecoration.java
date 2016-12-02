package com.letv.wallet.common.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

import com.letv.wallet.common.R;


/**
 * Created by linquan on 16-1-25.
 */
public class GridDividerItemDecoration extends RecyclerView.ItemDecoration {

    private static final int[] ATTRS = new int[]{android.R.attr.listDivider};
    private Paint mPaint;
    private int mColor;
    private int mDisColor;
    private int mSize;
    private boolean mHaveDisColor = false;

    public GridDividerItemDecoration(Context context, int color) {
        this(context, color, context.getResources().getDimensionPixelSize(R.dimen.divider_width));
    }

    public GridDividerItemDecoration(Context context, int color, int strokeWidth) {
        mPaint = new Paint();
        this.mColor = color;
        this.mSize = strokeWidth;
        mPaint.setStrokeWidth(mSize);
        mHaveDisColor = false;
    }

    public void setSecondColor(int color) {
        mDisColor = color;
        mHaveDisColor = true;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
    /*
    *    |__|__
    */
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                    .getLayoutParams();
            int spanCount = getSpanCount(parent);
            int color = mHaveDisColor ? child.isEnabled() ? mDisColor : mColor : mColor;
            mPaint.setColor(color);

            boolean top = isFirstRaw(parent, i, spanCount, childCount);
            boolean right = isLastColume(parent, i, spanCount, childCount);
            drawHorizential(c, child, params, top);
            drawVertical(c, child, params, right);
        }
    }

    private int getSpanCount(RecyclerView parent) {
        // 列数
        int spanCount = -1;
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {

            spanCount = ((GridLayoutManager) layoutManager).getSpanCount();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            spanCount = ((StaggeredGridLayoutManager) layoutManager)
                    .getSpanCount();
        }
        return spanCount;
    }

    /**
     * 设置分割线尺寸
     *
     * @param size 尺寸
     */
    public void setSize(int size) {
        this.mSize = size;
        mPaint.setStrokeWidth(mSize);
    }

    public void drawVertical(Canvas c, View child, RecyclerView.LayoutParams params,
                             boolean drawRight) {
        final int top = child.getTop() - params.topMargin;
        final int bottom = child.getBottom() + params.bottomMargin;

        final int left = child.getLeft() + params.rightMargin + mSize;
        c.drawLine(left, top, left, bottom, mPaint);

        if (drawRight) {
            final int rightR = child.getRight() + params.rightMargin - mSize;
            c.drawLine(rightR, top, rightR, bottom, mPaint);
        }
    }

    public void drawHorizential(Canvas c, View child,
                                RecyclerView.LayoutParams params, boolean drawTop) {
        final int left = child.getLeft() - params.leftMargin;
        final int right = child.getRight() + params.rightMargin;
        final int bottom = child.getBottom() + params.bottomMargin - mSize;
        c.drawLine(left, bottom, right, bottom, mPaint);

        if (drawTop) {
            final int topT = child.getTop() + params.topMargin + mSize;
            c.drawLine(left, topT, right, topT, mPaint);
        }

    }

    //return true; draw Right line
    private boolean isLastColume(RecyclerView parent, int pos, int spanCount,
                                 int childCount) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            if ((pos + 1) % spanCount == 0 || ((pos + 1) == childCount && (pos + 1) % spanCount != 0))// 如果是最后一列或最后一个item不在最后一列时，则需要绘制右边
            {
                return true;
            }
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            int orientation = ((StaggeredGridLayoutManager) layoutManager)
                    .getOrientation();
            if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                if ((pos + 1) % spanCount == 0)// 如果是最后一列，则需要绘制右边
                {
                    return true;
                }
            } else {
                childCount = childCount - childCount % spanCount;
                if (pos >= childCount)// 如果是最后一列，则需要绘制右边
                    return true;
            }
        }
        return false;
    }

    //return true : draw top line
    private boolean isFirstRaw(RecyclerView parent, int pos, int spanCount,
                               int childCount) {
        if (pos < spanCount)
            return true;

        return false;
    }

/*
    @Override
    public void getItemOffsets(Rect outRect, View v,
                               RecyclerView parent ,RecyclerView.State state) {
        int itemPosition = ((RecyclerView.LayoutParams) v.getLayoutParams()).getViewLayoutPosition();
        Drawable divider = v.isEnabled() ? mEnDivider : mDisDivider;
        int spanCount = getSpanCount(parent);
        int childCount = parent.getAdapter().getItemCount();
        int topLine = 0;
        int rightLine = 0;


        if (isFirstRaw(parent, itemPosition, spanCount, childCount))// 如果是第一行，要绘制上边
        {
            topLine = divider.getIntrinsicWidth();
        }
        if (isLastColume(parent, itemPosition, spanCount, childCount))// 如果是最后第一列，要绘制右边
        {
            rightLine = divider.getIntrinsicHeight();
        }
        outRect.set(topLine, rightLine, divider.getIntrinsicWidth(),
                divider.getIntrinsicHeight());
    }
    */

}
