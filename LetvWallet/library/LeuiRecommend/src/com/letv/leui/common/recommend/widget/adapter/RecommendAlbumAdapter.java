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
import com.letv.leui.common.recommend.widget.adapter.dto.RecommendAlbumDTO;
import com.letv.leui.common.recommend.widget.adapter.dto.RecommendAlbumDTOContent;
import com.letv.leui.common.recommend.widget.adapter.listener.RecommendImageListener;
import com.letv.leui.common.R;

import java.util.ArrayList;

/**
 * Created by zhangjiahao on 15-5-27.
 */
public class RecommendAlbumAdapter extends BaseRecommendAdapter<RecommendAlbumDTO, RecommendAlbumAdapter.RecommendAlbumViewHolder> {

    private LeRecommendViewStyle mCurViewStyle;

    public RecommendAlbumAdapter(ArrayList itemList, LeRecommendViewStyle style) {
        super(itemList);
        this.mCurViewStyle = style;
    }

    @Override
    protected int getItemLayoutResId() {
        return R.layout.item_recommend_album;
    }

    @Override
    RecommendAlbumViewHolder getViewHolder(RelativeLayout itemView) {
        return new RecommendAlbumViewHolder(itemView);
    }

    @Override
    float getScreenItemCount(int screenW, int parentPaddingLeft, int parentPaddingRight, int end, int start) {
        return 0;
    }

    @Override
    public void onBindViewHolder(RecommendAlbumViewHolder holder, int i) {
        holder.setItemState(false);
        holder.setTitleVisibility(true);
        RecommendAlbumDTO vo = mDataSet.get(i);
        if (holder.imageContainer != null) {
            holder.imageContainer.cancelRequest();
        }
        RecommendAlbumDTOContent content = vo.getContent();
        if (content != null) {
            holder.tv_musics_album_name.setText(content.getAlbum_name());
            holder.imageContainer = VolleyClient.loadImage(VolleyController.getInstance(mContext), content.getLogo(), new RecommendImageListener(holder.iv_album_photo, 0));
        }
    }

    public class RecommendAlbumViewHolder extends BaseRecommendAdapter.BaseRecommendViewHolder {

        ImageView iv_album_photo;
        TextView tv_musics_album_name;
        ImageLoader.ImageContainer imageContainer;
        private RelativeLayout rlContext, lrMoreItem;

        public RecommendAlbumViewHolder(RelativeLayout itemView) {
            super(itemView);
        }

        @Override
        protected void initItemView(RelativeLayout mItemView) {
            iv_album_photo = (ImageView) mItemView.findViewById(R.id.iv_album_photo);
            tv_musics_album_name = (TextView) mItemView.findViewById(R.id.tv_musics_album_name);

            rlContext = (RelativeLayout) mItemView.findViewById(R.id.rl_context_box);
            lrMoreItem = (RelativeLayout) mItemView.findViewById(R.id.item_recommend_more);
            if (mCurViewStyle == LeRecommendViewStyle.WHITE) {
                tv_musics_album_name.setTextColor(Color.WHITE);
//                mItemView.setBackgroundResource(R.color.item_recommend_card_background_color_white);
            }
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

        public void setTitleVisibility(boolean isVisibility) {
            int i = isVisibility ? View.VISIBLE : View.GONE;
            tv_musics_album_name.setVisibility(i);
        }
    }
}
