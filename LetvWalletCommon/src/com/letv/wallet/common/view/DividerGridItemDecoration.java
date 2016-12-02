package com.letv.wallet.common.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

import com.letv.wallet.common.util.DensityUtils;

/**
 * Created by liuliang on 16-3-23.
 */
public class DividerGridItemDecoration extends RecyclerView.ItemDecoration {

    private Paint mPaint;
    private int mColor;
    private int mDividerHeight;

    private boolean isDrawLeft = false;
    private boolean isDrawRight = false;
    private boolean isDrawTop = false;
    private boolean isDrawBottom = false;

    public DividerGridItemDecoration(Context context) {
        this(context, Color.WHITE, (int) DensityUtils.dip2px(1));
    }

    public DividerGridItemDecoration(Context context, int color, int dividerHeight) {
        mColor = color;
        mDividerHeight = dividerHeight;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(color);
        mPaint.setStrokeWidth(dividerHeight);
    }
    public void setDrawRect(boolean drawLeft, boolean drawRight, boolean drawTop, boolean drawBottom) {
        isDrawLeft = drawLeft;
        isDrawRight = drawRight;
        isDrawTop = drawTop;
        isDrawBottom = drawBottom;
    }

    public void updateDividerColor(int color) {
        mColor = color;
        mPaint.setColor(mColor);
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        drawHorizontal(c, parent);
        drawVertical(c, parent);
    }

    private int getSpanCount(RecyclerView parent) {
        // 列数
        int spanCount = -1;
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            spanCount = ((GridLayoutManager) layoutManager).getSpanCount();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            spanCount = ((StaggeredGridLayoutManager) layoutManager).getSpanCount();
        }
        return spanCount;
    }

    public void drawHorizontal(Canvas c, RecyclerView parent) {
        /*int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int left = child.getLeft() - params.leftMargin;
            final int right = child.getRight() + params.rightMargin + mDividerHeight;
            final int top = child.getBottom() + params.bottomMargin;
            final int bottom = top + mDividerHeight;
            c.drawLine(left, top, right, bottom, mPaint);
        }*/
        int childCount = parent.getChildCount();
        int spanCount = getSpanCount(parent);
        View child;
        RecyclerView.LayoutParams params;
        int left, right, top, bottom;

        for (int i = 0; i < childCount; i++) {
            child = parent.getChildAt(i);
            params = (RecyclerView.LayoutParams) child.getLayoutParams();

            left = child.getLeft() - params.leftMargin;
            if (isDrawLeft && isFirstColum(parent, i, spanCount, childCount)) {
                left -= mDividerHeight;
            }
            right = child.getRight() + params.rightMargin + mDividerHeight;
            if (!isDrawRight && isLastColum(parent, i, spanCount, childCount)) {
                right = child.getRight() + params.rightMargin;
            }
            top = child.getBottom() + params.bottomMargin;
            bottom = top + mDividerHeight;
            if (isDrawBottom || !isLastRaw(parent, i, spanCount, childCount)) {
                c.drawLine(left, top, right, bottom, mPaint);
            }

            //draw Top
            if (isDrawTop && isFirstRaw(parent, i, spanCount, childCount)) {
                top = child.getTop() - params.topMargin - mDividerHeight;
                bottom = top + mDividerHeight;
                c.drawLine(left, top, right, bottom, mPaint);
            }
        }
    }

    public void drawVertical(Canvas c, RecyclerView parent) {
        /*final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);

            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int top = child.getTop() - params.topMargin;
            final int bottom = child.getBottom() + params.bottomMargin;
            final int left = child.getRight() + params.rightMargin;
            final int right = left + mDividerHeight;

            c.drawLine(left, top, right, bottom, mPaint);
        }*/
        int childCount = parent.getChildCount();
        int spanCount = getSpanCount(parent);
        View child;
        RecyclerView.LayoutParams params;
        int left, right, top, bottom;

        for (int i = 0; i < childCount; i++) {
            child = parent.getChildAt(i);
            params = (RecyclerView.LayoutParams) child.getLayoutParams();

            top = child.getTop() - params.topMargin;
            bottom = child.getBottom() + params.bottomMargin;
            left = child.getRight() + params.rightMargin;
            right = left + mDividerHeight;

            if (isDrawRight || !isLastColum(parent, i, spanCount, childCount)) {
                c.drawLine(left, top, right, bottom, mPaint);
            }

            //draw left
            if (isDrawLeft && isFirstColum(parent, i, spanCount, childCount)) {
                left = child.getLeft() - params.leftMargin - mDividerHeight;
                right = left + mDividerHeight;
                c.drawLine(left, top, right, bottom, mPaint);
            }
        }
    }

    private boolean isFirstColum(RecyclerView parent, int pos, int spanCount, int childCount) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            if (pos % spanCount == 0) {
                return true;
            }
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            int orientation = ((StaggeredGridLayoutManager) layoutManager).getOrientation();
            if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                if (pos % spanCount == 0) {
                    return true;
                }
            } else {
                if (pos < spanCount) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isLastColum(RecyclerView parent, int pos, int spanCount, int childCount) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            // 如果是最后一列，则不需要绘制右边
            if ((pos + 1) % spanCount == 0) {
                return true;
            }
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            int orientation = ((StaggeredGridLayoutManager) layoutManager).getOrientation();
            if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                // 如果是最后一列，则不需要绘制右边
                if ((pos + 1) % spanCount == 0) {
                    return true;
                }
            } else {
                childCount = childCount - childCount % spanCount;
                if (pos >= childCount)// 如果是最后一列，则不需要绘制右边
                    return true;
            }
        }
        return false;
    }

    private boolean isFirstRaw(RecyclerView parent, int pos, int spanCount, int childCount) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            if (pos / spanCount == 0) {
                return true;
            }
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            int orientation = ((StaggeredGridLayoutManager) layoutManager).getOrientation();
            if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                if (pos / spanCount == 0) {
                    return true;
                }
            } else {
                if (pos % spanCount == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isLastRaw(RecyclerView parent, int pos, int spanCount, int childCount) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            int temp = childCount % spanCount;
            childCount = childCount - (temp == 0 ? spanCount : temp);
            if (pos >= childCount)// 如果是最后一行，则不需要绘制底部
                return true;
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            int orientation = ((StaggeredGridLayoutManager) layoutManager).getOrientation();
            // StaggeredGridLayoutManager 且纵向滚动
            if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                int temp = childCount % spanCount;
                childCount = childCount - (temp == 0 ? spanCount : temp);
                // 如果是最后一行，则不需要绘制底部
                if (pos >= childCount)
                    return true;
            } else {
                // StaggeredGridLayoutManager 且横向滚动
                // 如果是最后一行，则不需要绘制底部
                if ((pos + 1) % spanCount == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int spanCount = getSpanCount(parent);
        int childCount = parent.getAdapter().getItemCount();
        int position = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewLayoutPosition();
        int left = 0;
        if (isDrawLeft && isFirstColum(parent, position, spanCount, childCount)) {
            left = mDividerHeight;
        }
        int right = mDividerHeight;
        if (!isDrawRight && isLastColum(parent, position, spanCount, childCount)) {
            right = 0;
        }
        int top = 0;
        if (isDrawTop && isFirstRaw(parent, position, spanCount, childCount)) {
            top = mDividerHeight;
        }
        int bottom = mDividerHeight;
        if (!isDrawBottom && isLastRaw(parent, position, spanCount, childCount)) {
            bottom = 0;
        }
        outRect.set(left, top, right, bottom);
    }

    /*@Override
    public void getItemOffsets(Rect outRect, int itemPosition, RecyclerView parent) {
        int spanCount = getSpanCount(parent);
        int childCount = parent.getAdapter().getItemCount();
        // 如果是最后一行，则不需要绘制底部
        if (isLastRaw(parent, itemPosition, spanCount, childCount)) {
            outRect.set(0, 0, mDividerHeight, 0);
        } else if (isLastColum(parent, itemPosition, spanCount, childCount)) {
            // 如果是最后一列，则不需要绘制右边
            outRect.set(0, 0, 0, mDividerHeight);
        } else {
            outRect.set(0, 0, mDividerHeight, mDividerHeight);
        }
    }*/
}
