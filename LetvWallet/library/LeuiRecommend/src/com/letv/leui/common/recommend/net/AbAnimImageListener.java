package com.letv.leui.common.recommend.net;

import android.content.Context;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import com.letv.leui.common.recommend.volley.VolleyError;
import com.letv.leui.common.recommend.volley.toolbox.ImageLoader;

/**
 * Created by dupengtao on 2014/6/17.
 */
public abstract class AbAnimImageListener implements ImageLoader.ImageListener {


    private final Context mContext;
    private final ImageView mImageView;
    private final int mErrorResId;

    public AbAnimImageListener(Context context, ImageView imageView, int errorResId) {
        mContext = context;
        mImageView = imageView;
        mErrorResId = errorResId;
    }

    public AbAnimImageListener(Context context, ImageView imageView, int errorResId, int loadingResId) {
        this(context, imageView, errorResId);
        if (loadingResId > 0) {
            mImageView.setImageResource(loadingResId);
        }
    }

    @Override
    public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
        if (mImageView != null) {
            if (response.getBitmap() != null) {
                int animResId = getAnimResId();
                if (animResId > 0) {
                    mImageView.startAnimation(AnimationUtils.loadAnimation(mContext, animResId));
                }
                mImageView.setImageBitmap(response.getBitmap());
            } else {
                mImageView.setImageResource(mErrorResId);
            }
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        mImageView.setImageResource(mErrorResId);
    }

    public abstract int getAnimResId();
}
