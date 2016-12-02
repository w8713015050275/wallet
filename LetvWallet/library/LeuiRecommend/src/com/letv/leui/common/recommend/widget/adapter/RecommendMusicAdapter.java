package com.letv.leui.common.recommend.widget.adapter;


import android.graphics.Color;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.letv.leui.common.R;
import com.letv.leui.common.recommend.widget.LeRecommendViewStyle;
import com.letv.leui.common.recommend.widget.adapter.dto.RecommendMusicContent;
import com.letv.leui.common.recommend.widget.adapter.dto.RecommendMusicDTO;

import java.util.List;

/**
 * Created by dupengtao on 14-12-6.
 */
public class RecommendMusicAdapter extends BaseRecommendAdapter<RecommendMusicDTO, RecommendMusicAdapter.RecommendMusicViewHolder> {

    private LeRecommendViewStyle mCurViewStyle;

    public RecommendMusicAdapter(List<RecommendMusicDTO> dataSet) {
        this(dataSet, LeRecommendViewStyle.NORMAL);
    }

    public RecommendMusicAdapter(List<RecommendMusicDTO> dataSet, LeRecommendViewStyle style) {
        super(dataSet);
        mCurViewStyle = style;
    }

    @Override
    protected int getItemLayoutResId() {
        return R.layout.item_recommend_music;
    }

    @Override
    RecommendMusicAdapter.RecommendMusicViewHolder getViewHolder(RelativeLayout itemView) {
        return new RecommendMusicViewHolder(itemView);
    }

    @Override
    float getScreenItemCount(int screenW, int parentPaddingLeft, int parentPaddingRight, int end, int start) {
        return 0;
    }

    @Override
    public int getItemCount() {
        if (mDataSet.size() / 3 == 0) {
            return 1;
        }
        if (mDataSet.size() % 3 == 0) {
            return mDataSet.size() / 3;
        } else {
            return mDataSet.size() / 3 + 1;
        }
    }

    @Override
    public void onBindViewHolder(RecommendMusicAdapter.RecommendMusicViewHolder holder, int i) {
//        if (i > 2) {
//            holder.tvTopMusicIndex.setTextAppearance(mContext, R.style.LeTextStyleThin);
//            holder.tvCenterMusicIndex.setTextAppearance(mContext, R.style.LeTextStyleThin);
//            holder.tvBottomMusicIndex.setTextAppearance(mContext, R.style.LeTextStyleThin);
//        } else {
//            holder.tvTopMusicIndex.setTextAppearance(mContext, R.style.LeTextStyle);
//            holder.tvCenterMusicIndex.setTextAppearance(mContext, R.style.LeTextStyle);
//            holder.tvBottomMusicIndex.setTextAppearance(mContext, R.style.LeTextStyle);
//        }

        int columnCount = 3 - ((i + 1) * 3 > mDataSet.size() ? (i + 1) * 3 - mDataSet.size() : 0);
        int index;
        switch (columnCount) {
            case 3:
                holder.llMusicBottom.setVisibility(View.VISIBLE);
                holder.llMusicCenter.setVisibility(View.VISIBLE);

                RecommendMusicDTO vo = mDataSet.get(i * 3);
                RecommendMusicContent content = vo.getContent();
                index = i * 3 + 1;
                holder.tvTopMusicIndex.setText(index > 9 ? "" + index : "0" + index);
                holder.tvTopMusicName.setText(content.getSong_name());
                holder.tvTopMusicAlbum.setText(content.getAlbum_name());
                holder.llMusicTop.setTag(i * 3);

                vo = mDataSet.get(i * 3 + 1);
                content = vo.getContent();
                index = i * 3 + 2;
                holder.tvCenterMusicIndex.setText(index > 9 ? "" + index : "0" + index);
                holder.tvCenterMusicName.setText(content.getSong_name());
                holder.tvCenterMusicAlbum.setText(content.getAlbum_name());
                holder.llMusicCenter.setTag(i * 3 + 1);

                vo = mDataSet.get(i * 3 + 2);
                content = vo.getContent();
                index = i * 3 + 3;
                holder.tvBottomMusicIndex.setText(index > 9 ? "" + index : "0" + index);
                holder.tvBottomMusicName.setText(content.getSong_name());
                holder.tvBottomMusicAlbum.setText(content.getAlbum_name());
                holder.llMusicBottom.setTag(i * 3 + 2);
                break;
            case 2:
                holder.llMusicBottom.setVisibility(View.GONE);
                holder.llMusicCenter.setVisibility(View.VISIBLE);

                RecommendMusicDTO vo2 = mDataSet.get(i * 3);
                RecommendMusicContent content2 = vo2.getContent();
                index = i * 3 + 1;
                holder.tvTopMusicIndex.setText(index > 9 ? "" + index : "0" + index);
                holder.tvTopMusicName.setText(content2.getSong_name());
                holder.tvTopMusicAlbum.setText(content2.getAlbum_name());
                holder.llMusicTop.setTag(i * 3);

                vo2 = mDataSet.get(i * 3 + 1);
                content2 = vo2.getContent();
                index = i * 3 + 2;
                holder.tvCenterMusicIndex.setText(index > 9 ? "" + index : "0" + index);
                holder.tvCenterMusicName.setText(content2.getSong_name());
                holder.tvCenterMusicAlbum.setText(content2.getAlbum_name());
                holder.llMusicCenter.setTag(i * 3 + 1);
                break;
            case 1:
                holder.llMusicBottom.setVisibility(View.GONE);
                holder.llMusicCenter.setVisibility(View.GONE);

                RecommendMusicDTO vo3 = mDataSet.get(i * 3);
                RecommendMusicContent content3 = vo3.getContent();
                index = i * 3 + 1;
                holder.tvTopMusicIndex.setText(index > 9 ? "" + index : "0" + index);
                holder.tvTopMusicName.setText(content3.getSong_name());
                holder.tvTopMusicAlbum.setText(content3.getAlbum_name());
                holder.llMusicTop.setTag(i * 3);
                break;
        }
    }

    public class RecommendMusicViewHolder extends BaseRecommendAdapter.BaseRecommendViewHolder {
        TextView tvTopMusicIndex, tvCenterMusicIndex, tvBottomMusicIndex,
                tvTopMusicName, tvCenterMusicName, tvBottomMusicName,
                tvTopMusicAlbum, tvCenterMusicAlbum, tvBottomMusicAlbum;

        RelativeLayout llMusicTop, llMusicCenter, llMusicBottom;

        public RecommendMusicViewHolder(RelativeLayout itemView) {
            super(itemView);
        }

        @Override
        protected void initItemView(RelativeLayout mItemView) {
            llMusicTop = (RelativeLayout) mItemView.findViewById(R.id.ll_music_top);
            llMusicCenter = (RelativeLayout) mItemView.findViewById(R.id.ll_music_center);
            llMusicBottom = (RelativeLayout) mItemView.findViewById(R.id.ll_music_bottom);

            tvTopMusicIndex = (TextView) mItemView.findViewById(R.id.tv_music_index_top);
            tvCenterMusicIndex = (TextView) mItemView.findViewById(R.id.tv_music_index_center);
            tvBottomMusicIndex = (TextView) mItemView.findViewById(R.id.tv_music_index_bottom);

            tvTopMusicName = (TextView) mItemView.findViewById(R.id.tv_music_name_top);
            tvCenterMusicName = (TextView) mItemView.findViewById(R.id.tv_music_name_center);
            tvBottomMusicName = (TextView) mItemView.findViewById(R.id.tv_music_name_bottom);

            tvTopMusicAlbum = (TextView) mItemView.findViewById(R.id.tv_music_album_top);
            tvCenterMusicAlbum = (TextView) mItemView.findViewById(R.id.tv_music_album_center);
            tvBottomMusicAlbum = (TextView) mItemView.findViewById(R.id.tv_music_album_bottom);

            if (mCurViewStyle == LeRecommendViewStyle.WHITE) {
                tvTopMusicIndex.setTextColor(Color.WHITE);
                tvCenterMusicIndex.setTextColor(Color.WHITE);
                tvBottomMusicIndex.setTextColor(Color.WHITE);

//                tvTopMusicIndex.setBackgroundResource(R.color.item_recommend_card_background_color_white);
//                tvCenterMusicIndex.setBackgroundResource(R.color.item_recommend_card_background_color_white);
//                tvBottomMusicIndex.setBackgroundResource(R.color.item_recommend_card_background_color_white);

                tvTopMusicName.setTextColor(Color.WHITE);
                tvCenterMusicName.setTextColor(Color.WHITE);
                tvBottomMusicName.setTextColor(Color.WHITE);

                tvTopMusicAlbum.setTextColor(Color.parseColor("#999999"));
                tvCenterMusicAlbum.setTextColor(Color.parseColor("#999999"));
                tvBottomMusicAlbum.setTextColor(Color.parseColor("#999999"));

//                mItemView.findViewById(R.id.rl_music_box_top).setBackgroundResource(R.color.item_recommend_card_background_color_white);
//                mItemView.findViewById(R.id.rl_music_box_center).setBackgroundResource(R.color.item_recommend_card_background_color_white);
//                mItemView.findViewById(R.id.rl_music_box_bottom).setBackgroundResource(R.color.item_recommend_card_background_color_white);
            }
        }

        @Override
        protected void setListener() {
            llMusicTop.setOnClickListener(this);
            llMusicCenter.setOnClickListener(this);
            llMusicBottom.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int id = v.getId();
            int tag = 0;
            if (id == R.id.ll_music_top) {
                tag = (int) llMusicTop.getTag();
            } else if (id == R.id.ll_music_center) {
                tag = (int) llMusicCenter.getTag();
            } else if (id == R.id.ll_music_bottom) {
                tag = (int) llMusicBottom.getTag();
            }
            if (itemClickListener != null) {
                itemClickListener.onItemClick(v, tag, getItemCount());
            }
        }
    }
}
