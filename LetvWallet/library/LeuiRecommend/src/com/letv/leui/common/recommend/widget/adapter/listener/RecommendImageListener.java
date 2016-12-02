package com.letv.leui.common.recommend.widget.adapter.listener;

import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import com.letv.leui.common.recommend.volley.VolleyError;
import com.letv.leui.common.recommend.volley.toolbox.ImageLoader;
import com.letv.leui.common.R;

/**
 * Created by dupengtao on 14-12-8.
 */
public class RecommendImageListener implements ImageLoader.ImageListener {

    private int mState;
    private ImageView view;

    public RecommendImageListener(ImageView view) {
        this.view = view;
    }

    public RecommendImageListener(ImageView view, int state) {
        this.view = view;
        mState = state;
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        view.setImageDrawable(null);
        if (mState == 1) {
            view.setBackgroundResource(R.drawable.recommend_load_error);
        } else {
            view.setBackgroundColor(view.getResources().getColor(R.color.item_recommend_photo_bg));
        }
    }

    @Override
    public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
        if (view != null) {
            if (response.getBitmap() != null) {
                view.startAnimation(AnimationUtils.loadAnimation(view.getContext(), R.anim.fade_in));
                view.setImageBitmap(response.getBitmap());
            } else {
                view.setImageDrawable(null);
                view.setBackgroundColor(view.getResources().getColor(R.color.item_recommend_photo_bg));
            }
        }
    }
}
