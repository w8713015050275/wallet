package com.letv.wallet.common.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.letv.wallet.common.R;

/**
 * Created by linquan on 15-11-25.
 */
public class DividerItemDecoration extends RecyclerView.ItemDecoration {

    public static final int HORIZONTAL_LIST = LinearLayoutManager.HORIZONTAL;
    public static final int VERTICAL_LIST = LinearLayoutManager.VERTICAL;
    private int mOrientation;
    private Paint mPaint;
    private int mColor;
    private int mSize;
    private boolean mHeadLine = false;
    private int mHeadSize;
    private Context mContext;

    public DividerItemDecoration(Context context, int color) {
        this(context, color, VERTICAL_LIST);
    }

    public DividerItemDecoration(Context context, int color, int orientation) {
        this(context, color, VERTICAL_LIST, context.getResources().getDimensionPixelSize(R.dimen.divider_width));
    }

    public DividerItemDecoration(Context context, int color, int orientation, int strokeWidth) {
        mContext = context;
        this.mOrientation = orientation;
        this.mColor = color;
        this.mSize = strokeWidth;
        mPaint = new Paint();
        mPaint.setColor(mColor);
        mPaint.setStrokeWidth(mSize);
    }

    public void setOrientation(int orientation) {
        if (orientation != HORIZONTAL_LIST && orientation != VERTICAL_LIST) {
            throw new IllegalArgumentException("invalid orientation");
        }
        mOrientation = orientation;
    }

    public void setTopAndBottomLine(boolean headLine, int headLineSize) {
        mHeadLine = headLine;
        if (mHeadLine && headLineSize == 0) {
            mHeadSize = mContext.getResources().getDimensionPixelSize(R.dimen.divider_width);
        } else {
            mHeadSize = headLineSize;
        }
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent) {
        if (mOrientation == VERTICAL_LIST) {
            drawVertical(c, parent);
        } else {
            drawHorizontal(c, parent);
        }
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

    protected void drawHorizontal(Canvas c, RecyclerView parent) {
        final int top = parent.getPaddingTop();
        final int bottom = parent.getHeight() - parent.getPaddingBottom();

        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int right = child.getRight() + params.rightMargin - mSize;
            c.drawLine(right, top, right, bottom, mPaint);
            if (i == 0 && mHeadLine) {
                final int left = child.getLeft() + params.topMargin + mHeadSize;
                c.drawLine(left, top, right, top, mPaint);
            }
        }
    }

    public void drawVertical(Canvas c, RecyclerView parent) {
        final int left = parent.getPaddingLeft();
        final int right = parent.getWidth() - parent.getPaddingRight();

        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int bottom = child.getBottom() + params.bottomMargin - mSize;

            c.drawLine(left, bottom, right, bottom, mPaint);
            if (i == 0 && mHeadLine) {
                final int top = child.getTop() + params.topMargin + mHeadSize;
                c.drawLine(left, top, right, top, mPaint);
            }
        }
    }
}