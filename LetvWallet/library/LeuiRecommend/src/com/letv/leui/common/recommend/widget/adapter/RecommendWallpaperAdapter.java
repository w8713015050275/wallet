package com.letv.leui.common.recommend.widget.adapter;


import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.letv.leui.common.R;
import com.letv.leui.common.recommend.net.VolleyClient;
import com.letv.leui.common.recommend.net.VolleyController;
import com.letv.leui.common.recommend.volley.toolbox.ImageLoader;
import com.letv.leui.common.recommend.widget.adapter.dto.RecommendWallpaperDTO;
import com.letv.leui.common.recommend.widget.adapter.listener.RecommendImageListener;

import java.util.List;

/**
 * Created by dupengtao on 14-12-6.
 */
public class RecommendWallpaperAdapter extends BaseRecommendAdapter<RecommendWallpaperDTO, RecommendWallpaperAdapter.RecommendWallpaperViewHolder> {


    public RecommendWallpaperAdapter(List<RecommendWallpaperDTO> dataSet) {
        super(dataSet);
    }

    @Override
    protected int getItemLayoutResId() {
        return R.layout.item_recommend_wallpaper;
    }

    @Override
    RecommendWallpaperViewHolder getViewHolder(RelativeLayout itemView) {
        return new RecommendWallpaperViewHolder(itemView);
    }

    @Override
    float getScreenItemCount(int screenW, int parentPaddingLeft, int parentPaddingRight, int end, int start) {
        return 0;
    }

    @Override
    public void onBindViewHolder(RecommendWallpaperViewHolder holder, int i) {
        holder.setItemState(false);
        RecommendWallpaperDTO vo = mDataSet.get(i);
        if (holder.imageContainer != null) {
            holder.imageContainer.cancelRequest();
        }
        String thumbnail = null;
        try {
            thumbnail = vo.getContent().getThumbnail();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(thumbnail)) {
            holder.imageContainer = VolleyClient.loadImage(VolleyController.getInstance(mContext), thumbnail, new RecommendImageListener(holder.ivPhoto, 1));
        }
    }

    public class RecommendWallpaperViewHolder extends BaseRecommendAdapter.BaseRecommendViewHolder {
        ImageView ivPhoto;
        ImageLoader.ImageContainer imageContainer;
        RelativeLayout rlContext, lrMoreItem;


        public RecommendWallpaperViewHolder(RelativeLayout itemView) {
            super(itemView);
        }

        @Override
        protected void initItemView(RelativeLayout mItemView) {
            ivPhoto = (ImageView) mItemView.findViewById(R.id.iv_wallpaper_photo);
            rlContext = (RelativeLayout) mItemView.findViewById(R.id.rl_context_box);
            lrMoreItem = (RelativeLayout) mItemView.findViewById(R.id.item_recommend_more);
        }
        public void setItemState(boolean isMoreItem) {
            if (isMoreItem) {
                lrMoreItem.setVisibility(View.VISIBLE);
                rlContext.setVisibility(View.GONE);
            } else {
                lrMoreItem.setVisibility(View.GONE);
                rlContext.setVisibility(View.VISIBLE);
            }
        }
    }
}
