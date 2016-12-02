package com.letv.leui.common.recommend.widget.adapter;

import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.letv.leui.common.recommend.volley.toolbox.ImageLoader;
import com.letv.leui.common.R;
import com.letv.leui.common.recommend.net.VolleyClient;
import com.letv.leui.common.recommend.net.VolleyController;
import com.letv.leui.common.recommend.widget.adapter.vo.DemoAdapterItem;

import java.util.List;

/**
 * Created by dupengtao on 14-12-4.
 */
public class DemoRecommendAdapter extends BaseRecommendAdapter<DemoAdapterItem, DemoRecommendAdapter.DemoViewHolder> {


    public DemoRecommendAdapter(List<DemoAdapterItem> dataSet) {
        super(dataSet);
    }

    @Override
    protected int getItemLayoutResId() {
        return R.layout.item_recommend_music;
    }

    @Override
    DemoViewHolder getViewHolder(RelativeLayout itemView) {
        return new DemoViewHolder(itemView);
    }

    @Override
    float getScreenItemCount(int screenW, int parentPaddingLeft, int parentPaddingRight, int end, int start) {
        return 0;
    }

    @Override
    public void onBindViewHolder(DemoViewHolder demoViewHolder, int i) {
        DemoAdapterItem item = mDataSet.get(i);
        demoViewHolder.setSongName(item.getItemName());
        demoViewHolder.setAlbumName(item.getItemName());
        if (demoViewHolder.imageContainer != null) {
            demoViewHolder.imageContainer.cancelRequest();
        }
        demoViewHolder.imageContainer = VolleyClient.loadImage(VolleyController.getInstance(mContext), item.getItemUrl(), demoViewHolder.ivPhoto, R.drawable.ic_launcher, R.drawable.ic_launcher);
    }

    public class DemoViewHolder extends BaseRecommendAdapter.BaseRecommendViewHolder {

        TextView tvSongName;
        TextView tvAlbumName;
        ImageView ivPhoto;
        ImageLoader.ImageContainer imageContainer;

        public DemoViewHolder(RelativeLayout itemView) {
            super(itemView);
        }

        @Override
        protected void initItemView(RelativeLayout mItemView) {
//            tvSongName = (TextView) mItemView.findViewById(R.id.tv_song_name);
//            tvAlbumName = (TextView) mItemView.findViewById(R.id.tv_album_name);
//            ivPhoto = (ImageView) mItemView.findViewById(R.id.iv_photo);
        }

        public void setSongName(String text) {
            tvSongName.setText(text);
        }

        public void setAlbumName(String text) {
            tvAlbumName.setText(text);
        }
    }
}
