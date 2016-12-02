package com.letv.walletbiz.movie.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by changjiajie on 16-2-7.
 */
public class MovieSeatThumbV extends View {

    private Bitmap mThumbBitmap;
    private Paint mThumbBgPaint;

    public MovieSeatThumbV(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MovieSeatThumbV(Context context) {
        super(context, null);
    }

    public MovieSeatThumbV(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public void initV() {
        if (this.mThumbBgPaint == null) {
            this.mThumbBgPaint = new Paint();
            this.mThumbBgPaint.setColor(-1);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mThumbBitmap != null) {
            canvas.drawBitmap(this.mThumbBitmap, 0.0F, 0.0F, this.mThumbBgPaint);
        }
    }

    public void setBitmap(Bitmap bitmap) {
        this.mThumbBitmap = bitmap;
    }
}
