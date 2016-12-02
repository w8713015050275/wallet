package com.letv.leui.common.recommend.widget.adapter;


import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.letv.leui.common.R;
import com.letv.leui.common.recommend.net.VolleyClient;
import com.letv.leui.common.recommend.net.VolleyController;
import com.letv.leui.common.recommend.volley.toolbox.ImageLoader;
import com.letv.leui.common.recommend.widget.LeRecommendViewStyle;
import com.letv.leui.common.recommend.widget.adapter.dto.RecommendVideoContent;
import com.letv.leui.common.recommend.widget.adapter.dto.RecommendVideoDTO;
import com.letv.leui.common.recommend.widget.adapter.listener.RecommendImageListener;

import java.util.List;

/**
 * Created by dupengtao on 14-12-6.
 */
public class RecommendLatestNewsAdapter extends BaseRecommendAdapter<RecommendVideoDTO, RecommendLatestNewsAdapter.RecommendLatestNewsViewHolder> {
    private LeRecommendViewStyle mCurViewStyle;
    public RecommendLatestNewsAdapter(List<RecommendVideoDTO> dataSet) {
        this(dataSet, LeRecommendViewStyle.NORMAL);
    }

    public RecommendLatestNewsAdapter(List<RecommendVideoDTO> dataSet,LeRecommendViewStyle style) {
        super(dataSet);
        mCurViewStyle=style;
    }

    @Override
    protected int getItemLayoutResId() {
        return R.layout.item_recommend_latest_news;
    }

    @Override
    RecommendLatestNewsViewHolder getViewHolder(RelativeLayout itemView) {
        return new RecommendLatestNewsViewHolder(itemView);
    }

    @Override
    float getScreenItemCount(int screenW, int parentPaddingLeft, int parentPaddingRight, int end, int start) {
        return 0f;
    }

    @Override
    public void onBindViewHolder(RecommendLatestNewsViewHolder holder, int i) {
        if (mHasMoreItem&&i == getItemCount() - 1) {
            holder.setItemState(true);
            holder.setLatestNewsTitleVisibility(false);
        } else {
            holder.setItemState(false);
            holder.setLatestNewsTitleVisibility(true);
            RecommendVideoDTO vo = mDataSet.get(i);
            RecommendVideoContent content = vo.getContent();
            if(content!=null) {
                holder.setLatestNewsTitle(content.getVid_name());
                if (holder.imageContainer != null) {
                    holder.imageContainer.cancelRequest();
                }
                holder.imageContainer = VolleyClient.loadImage(VolleyController.getInstance(mContext), content.getVid_pic(), new RecommendImageListener(holder.ivPhoto, 0));
            }
        }
    }

    @Override
    public int getItemCount() {
        mHasMoreItem =false;
        int size = mDataSet.size();
        //mHasMoreItem = size >= MAX_ITEM_NUM;
        //int dataSize = mHasMoreItem ? 16 : size;
        return size;
    }

    public class RecommendLatestNewsViewHolder extends BaseRecommendAdapter.BaseRecommendViewHolder {
        TextView tvLatestNewsTitle;
        ImageView ivPhoto;
        ImageLoader.ImageContainer imageContainer;
        RelativeLayout rlContext, lrMoreItem;

        public RecommendLatestNewsViewHolder(RelativeLayout itemView) {
            super(itemView);
        }

        @Override
        protected void initItemView(RelativeLayout mItemView) {
            tvLatestNewsTitle = (TextView) mItemView.findViewById(R.id.tv_latest_news_title);
            ivPhoto = (ImageView) mItemView.findViewById(R.id.iv_latest_news_photo);
            mItemView.findViewById(R.id.iv_latest_news_play).setVisibility(View.INVISIBLE);
            rlContext = (RelativeLayout) mItemView.findViewById(R.id.rl_context_box);
            lrMoreItem = (RelativeLayout) mItemView.findViewById(R.id.item_recommend_more);

            if(mCurViewStyle==LeRecommendViewStyle.WHITE){
                tvLatestNewsTitle.setTextColor(Color.WHITE);
//                mItemView.setBackgroundResource(R.color.item_recommend_card_background_color_white);
            }
        }

        public void setLatestNewsTitle(String text) {
            tvLatestNewsTitle.setText(text);
        }

        public void setLatestNewsTitleVisibility(boolean isVisibility) {
            int i = isVisibility? View.VISIBLE:View.GONE;
            tvLatestNewsTitle.setVisibility(i);
        }

        public void setItemState(boolean isMoreItem){
            if(isMoreItem){
                lrMoreItem.setVisibility(View.VISIBLE);
                rlContext.setVisibility(View.GONE);
            }else {
                lrMoreItem.setVisibility(View.GONE);
                rlContext.setVisibility(View.VISIBLE);
            }
        }
    }
}
