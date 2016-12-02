package com.letv.leui.common.recommend.widget.adapter.listener;

import android.content.Context;
import android.view.View;
import com.letv.leui.common.recommend.widget.LeRecommendType;
import com.letv.leui.common.recommend.widget.adapter.dto.RecommendArtistsDTO;

import java.util.List;

/**
 * Created by zhangjiahao on 15-7-23.
 */
public abstract class OnArtistsItemClickListener extends ItemClickListener {

    private List<RecommendArtistsDTO> mArtistsData;

    public void setmAlbumData(List<RecommendArtistsDTO> mArtistsData) {
        this.mArtistsData = mArtistsData;
    }

    public OnArtistsItemClickListener(Context context) {
        super(context, LeRecommendType.ARTISTS);
    }

    @Override
    public void onItemClick(View view, int position, int count) {
        super.onItemClick(view, position, count);
        onArtistsItemClick(view, mArtistsData.get(position));
    }

    @Override
    public void onLabelActionClick(View view) {
        super.onLabelActionClick(view);
        onArtistsMoreActionClick(view);
    }

    /**
     * called when artists item clicked.
     *
     * @param view
     * @param artistsDTO artists info
     */
    public abstract void onArtistsItemClick(View view, RecommendArtistsDTO artistInfo);

    /**
     * called when artists more button clicked.
     *
     * @param view
     */
    public void onArtistsMoreActionClick(View view) {
    }

}
