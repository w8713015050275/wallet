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
import com.letv.leui.common.recommend.widget.adapter.dto.RecommendHotDTOItem;
import com.letv.leui.common.recommend.widget.adapter.listener.RecommendImageListener;

import java.util.List;

/**
 * Created by dupengtao on 14-12-6.
 */
public class RecommendHotAdapter extends BaseRecommendAdapter<RecommendHotDTOItem, RecommendHotAdapter.RecommendHotViewHolder> {

    private LeRecommendViewStyle mCurViewStyle;

    public RecommendHotAdapter(List<RecommendHotDTOItem> dataSet) {
        this(dataSet, LeRecommendViewStyle.NORMAL);
    }

    public RecommendHotAdapter(List<RecommendHotDTOItem> dataSet,LeRecommendViewStyle style) {
        super(dataSet);
        mCurViewStyle =style;
    }

    @Override
    protected int getItemLayoutResId() {
        return R.layout.item_recommend_hot_product;
    }

    @Override
    RecommendHotViewHolder getViewHolder(RelativeLayout itemView) {
        return new RecommendHotViewHolder(itemView);
    }

    @Override
    float getScreenItemCount(int screenW, int parentPaddingLeft, int parentPaddingRight, int end, int start) {
        return 0f;
    }

    @Override
    public void onBindViewHolder(final RecommendHotViewHolder holder, int i) {
        RecommendHotDTOItem vo = mDataSet.get(i);
        // holder.setHotFractionText(vo.getScore());
        holder.setHotProductText(vo.getContent().getPid_name());
        String hotProductText = vo.getContent().getCid_name() + ("tv".equals(vo.getContent().getChannel()) ? vo.getContent().getTotalepisode() + "集" : "");
        holder.setHotTypeText(hotProductText);
        holder.setHotFractionText(vo.getScore());
        if (holder.imageContainer != null) {
            holder.imageContainer.cancelRequest();
        }
        String url = vo.getContent().getPid_pic();
        holder.imageContainer = VolleyClient.loadImage(VolleyController.getInstance(mContext), url, new RecommendImageListener(holder.ivHotPhoto, 1));
    }

    @Override
    public int getItemCount() {
        mHasMoreItem =false;
        int size = mDataSet.size();
        //mHasMoreItem = size >= MAX_ITEM_NUM;
        //int dataSize = mHasMoreItem ? 16 : size;
        return size;
    }

    public class RecommendHotViewHolder extends BaseRecommendAdapter.BaseRecommendViewHolder {

        TextView tvHotType, tvHotFraction, tvHotProduct;
        ImageView ivHotPhoto;
        ImageLoader.ImageContainer imageContainer;
        RelativeLayout rlContext, lrMoreItem;

        public RecommendHotViewHolder(RelativeLayout itemView) {
            super(itemView);
        }

        @Override
        protected void initItemView(RelativeLayout mItemView) {
            tvHotType = (TextView) mItemView.findViewById(R.id.tv_hot_type);
            tvHotFraction = (TextView) mItemView.findViewById(R.id.tv_hot_fraction);
            tvHotProduct = (TextView) mItemView.findViewById(R.id.tv_hot_product);
            ivHotPhoto = (ImageView) mItemView.findViewById(R.id.iv_hot_photo);
            rlContext = (RelativeLayout) mItemView.findViewById(R.id.rl_context_box);
            lrMoreItem = (RelativeLayout) mItemView.findViewById(R.id.item_recommend_more);

            if(mCurViewStyle==LeRecommendViewStyle.WHITE){
                tvHotProduct.setTextColor(Color.parseColor("#ffffff"));
                tvHotType.setTextColor(Color.parseColor("#999999"));
                tvHotFraction.setTextColor(Color.parseColor("#ff7243"));
                mItemView.findViewById(R.id.rl_content_box).setBackgroundResource(R.color.item_recommend_card_background_color_white);
            }

        }

        public void setHotTypeText(String text) {
            tvHotType.setText(text);
        }

        public void setHotFractionText(String text) {
            tvHotFraction.setText(text);
        }

        public void setHotProductText(String text) {
            tvHotProduct.setText(text);
        }

        public void setHotProductTextVisibility(boolean isVisibility) {
            int i = isVisibility?View.VISIBLE:View.GONE;
            tvHotProduct.setVisibility(i);
        }

        public void setItemState(boolean isMoreItem){
            if(isMoreItem){
                lrMoreItem.setVisibility(View.GONE);
                rlContext.setVisibility(View.VISIBLE);
            }else {
                lrMoreItem.setVisibility(View.GONE);
                rlContext.setVisibility(View.VISIBLE);
            }
        }
    }
}
