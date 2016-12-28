package com.letv.walletbiz.movie.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Matrix;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Transformation;
import android.widget.Gallery;

import com.letv.walletbiz.R;

/**
 * Created by liuliang on 16-1-26.
 */
public class MovieGallery extends Gallery {

    /**
     * 未选中item通明度, 1为不通明
     */
    private float unselectedAlpha;
    private static final float DEFAULT_ALPHA = 0.6f;
    /**
     * 未选中item 进行缩放, 1为不缩放
     */
    private float unselectedScale;
    private static final float DEFAULT_SCALE = 0.75f;

    private Matrix mMatrix;

    private int coverCenter;

    public MovieGallery(Context context) {
        this(context, null);
    }

    public MovieGallery(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MovieGallery(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.MovieGallery);
        this.unselectedAlpha = a.getFloat(R.styleable.MovieGallery_unselected_alpha, DEFAULT_ALPHA);
        this.unselectedScale = a.getFloat(R.styleable.MovieGallery_unselected_scale, DEFAULT_SCALE);
        a.recycle();
        setStaticTransformationsEnabled(true);
    }

    @Override
    protected boolean getChildStaticTransformation(View child, Transformation t) {

        final int childWidth = child.getWidth();
        final int childCenter = child.getLeft() + childWidth / 2;

        final float effectsScale = getEffectsScale(coverCenter - childWidth, childCenter - coverCenter);

        t.clear();
        t.setTransformationType(Transformation.TYPE_BOTH);

        // Alpha
        if (this.unselectedAlpha != 1) {
            final float alphaAmount = (this.unselectedAlpha - 1) * Math.abs(effectsScale) + 1;
            t.setAlpha(alphaAmount);
        }

        // Zoom.
        if (this.unselectedScale != 1) {
            mMatrix = t.getMatrix();
            final float zoomScale = (this.unselectedScale - 1) * Math.abs(effectsScale) + 1;
            final float translateX = child.getWidth() / 2.0f;
            final float translateY = child.getHeight() / 2.0f;
            mMatrix.preTranslate(-translateX, -translateY);
            mMatrix.postScale(zoomScale, zoomScale);
            mMatrix.postTranslate(translateX, translateY);
        }

        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        super.onFling(e1, e2, 5.0F, velocityY);
        return false;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        coverCenter = getCenterOfCoverflow();
        super.onSizeChanged(w, h, oldw, oldh);
    }

    private int getCenterOfCoverflow() {
        return (getWidth() - getPaddingLeft() - getPaddingRight()) / 2 + getPaddingLeft();
    }

    private float getEffectsScale(int actionDistance, int effDistance) {
        if (actionDistance == 0)
            return 0;

        return Math.min(1.0f, Math.max(-1.0f, (1.0f / actionDistance) * (effDistance)));
    }

}
