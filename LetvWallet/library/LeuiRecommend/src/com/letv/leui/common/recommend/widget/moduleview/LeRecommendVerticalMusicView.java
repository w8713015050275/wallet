package com.letv.leui.common.recommend.widget.moduleview;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.letv.leui.common.recommend.widget.LeRecommendViewStyle;
import com.letv.leui.common.recommend.widget.adapter.dto.RecommendMusicContent;
import com.letv.leui.common.recommend.widget.adapter.dto.RecommendMusicDTO;
import com.letv.leui.common.R;
import com.letv.leui.common.recommend.widget.adapter.listener.BaseItemClickListener;

import java.util.ArrayList;

/**
 * Created by zhangjiahao on 15-9-1.
 */
public class LeRecommendVerticalMusicView extends LeRecommendVerticalView<LeRecommendVerticalMusicView.MusicViewHolder, RecommendMusicDTO> {

    public LeRecommendVerticalMusicView(Context context, int count) {
        super(context, count);
    }

    @Override
    protected ViewGroup createItemView(LayoutInflater inflater) {
        LinearLayout inflate = (LinearLayout) inflater.inflate(R.layout.item_recommend_music_vertical, ll_rootView, false);
        return inflate;
    }

    @Override
    protected void setDataForItemClickListener(BaseItemClickListener baseItemClickListener, ArrayList<RecommendMusicDTO> itemList) {
        baseItemClickListener.setMusicList(itemList);
    }

    @Override
    protected MusicViewHolder getViewHolder(ViewGroup itemView) {
        return new MusicViewHolder(itemView);
    }

    @Override
    protected void onBindViewHolder(MusicViewHolder holder, int i, ArrayList<RecommendMusicDTO> list, LeRecommendViewStyle style) {
        if (style == LeRecommendViewStyle.WHITE) {
            holder.tv_music_index.setTextColor(Color.WHITE);
//            holder.tv_music_index.setBackgroundResource(R.color.item_recommend_card_background_color_white);

            holder.tv_music_name.setTextColor(Color.WHITE);
            holder.tv_music_album.setTextColor(Color.parseColor("#999999"));

//            holder.rl_music_box.setBackgroundResource(R.color.item_recommend_card_background_color_white);
        }

        RecommendMusicDTO recommendMusicDTO = list.get(i);
        RecommendMusicContent content = recommendMusicDTO.getContent();

        holder.tv_music_index.setText("0" + (i + 1));
        holder.tv_music_name.setText(content.getSong_name());
        holder.tv_music_album.setText(content.getAlbum_name());
    }

    public class MusicViewHolder extends LeRecommendVerticalView.BaseViewHolder {

        TextView tv_music_index, tv_music_name, tv_music_album;
        RelativeLayout rl_music_box;

        public MusicViewHolder(ViewGroup viewGroup) {
            super(viewGroup);
        }

        @Override
        protected void initViewHolder(ViewGroup viewGroup) {
            tv_music_index = (TextView) viewGroup.findViewById(R.id.tv_music_index);
            tv_music_name = (TextView) viewGroup.findViewById(R.id.tv_music_name);
            tv_music_album = (TextView) viewGroup.findViewById(R.id.tv_music_album);
            rl_music_box = (RelativeLayout) viewGroup.findViewById(R.id.rl_music_box);
        }
    }
}
