package com.letv.leui.common.recommend.widget.adapter;

import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.letv.leui.common.recommend.net.VolleyClient;
import com.letv.leui.common.recommend.net.VolleyController;
import com.letv.leui.common.recommend.volley.toolbox.ImageLoader;
import com.letv.leui.common.recommend.widget.LeRecommendViewStyle;
import com.letv.leui.common.recommend.widget.adapter.dto.RecommendArtistsDTO;
import com.letv.leui.common.recommend.widget.adapter.listener.OnArtistsItemClickListener;
import com.letv.leui.common.recommend.widget.adapter.listener.OnBaseItemClickListener;
import com.letv.leui.common.recommend.widget.adapter.listener.RecommendImageListener;
import com.letv.leui.common.R;

import java.util.ArrayList;

/**
 * Created by zhangjiahao on 15-5-28.
 */
public class RecommendArtistsAdapter extends BaseRecommendAdapter<RecommendArtistsDTO, RecommendArtistsAdapter.RecommendArtistsViewHolder> {

    private LeRecommendViewStyle mCurViewStyle;

    public RecommendArtistsAdapter(ArrayList itemList, LeRecommendViewStyle style) {
        super(itemList);
        this.mCurViewStyle = style;
    }

    @Override
    protected int getItemLayoutResId() {
        return R.layout.item_recommend_artists;
    }

    @Override
    RecommendArtistsViewHolder getViewHolder(RelativeLayout itemView) {
        return new RecommendArtistsViewHolder(itemView);
    }

    @Override
    float getScreenItemCount(int screenW, int parentPaddingLeft, int parentPaddingRight, int end, int start) {
        return 0;
    }

    @Override
    public void onBindViewHolder(RecommendArtistsViewHolder holder, int i) {
        RecommendArtistsDTO vo = mDataSet.get(i);
        if (holder.imageContainer != null) {
            holder.imageContainer.cancelRequest();
        }
        if (vo != null) {
            holder.tv_artists_album_name.setText(vo.getName());
            holder.imageContainer = VolleyClient.loadImage(VolleyController.getInstance(mContext), vo.getLogo(), new RecommendImageListener(holder.iv_artists_photo, 0));
        }
    }

    @Override
    public void setItemClickListener(OnBaseItemClickListener itemClickListener) {
        super.setItemClickListener(itemClickListener);
        if (itemClickListener instanceof OnArtistsItemClickListener) {
            OnArtistsItemClickListener listener = (OnArtistsItemClickListener)itemClickListener;
            listener.setmAlbumData(mDataSet);
        }
    }

    public class RecommendArtistsViewHolder extends BaseRecommendAdapter.BaseRecommendViewHolder {

        ImageLoader.ImageContainer imageContainer;
        ImageView iv_artists_photo;
        TextView tv_artists_album_name;

        private RelativeLayout rlContext;
        private RelativeLayout lrMoreItem;

        public RecommendArtistsViewHolder(RelativeLayout itemView) {
            super(itemView);
        }

        @Override
        protected void initItemView(RelativeLayout mItemView) {
            iv_artists_photo = (ImageView) mItemView.findViewById(R.id.iv_artists_photo);
            tv_artists_album_name = (TextView) mItemView.findViewById(R.id.tv_artists_album_name);

            rlContext = (RelativeLayout) mItemView.findViewById(R.id.rl_context_box);
            lrMoreItem = (RelativeLayout) mItemView.findViewById(R.id.item_recommend_more);
            if (mCurViewStyle == LeRecommendViewStyle.WHITE) {
                tv_artists_album_name.setTextColor(Color.WHITE);
            }
        }

        public void setItemState(boolean isMoreItem) {
            if (isMoreItem) {
                lrMoreItem.setVisibility(View.GONE);
                rlContext.setVisibility(View.VISIBLE);
            } else {
                lrMoreItem.setVisibility(View.GONE);
                rlContext.setVisibility(View.VISIBLE);
            }
        }

        public void setTitleVisibility(boolean isVisibility) {
            int i = isVisibility ? View.VISIBLE : View.GONE;
            tv_artists_album_name.setVisibility(i);
        }
    }
}
