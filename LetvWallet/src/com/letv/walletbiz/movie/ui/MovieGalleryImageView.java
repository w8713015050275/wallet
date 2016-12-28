package com.letv.walletbiz.movie.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by lijunying on 16-12-28.
 */

public class MovieGalleryImageView extends ImageView {
    public MovieGalleryImageView(Context context) {
        super(context);
    }

    public MovieGalleryImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MovieGalleryImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MovieGalleryImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void offsetLeftAndRight(int offset) {
        super.offsetLeftAndRight(offset);
        // required to get children to draw correctly without drawing loop
        if (android.os.Build.VERSION.SDK_INT >= 16){
            invalidate();
        }
    }
}
