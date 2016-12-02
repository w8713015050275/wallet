package com.letv.leui.common.recommend.widget.moduleview;

import android.content.Context;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import com.letv.leui.common.R;
import com.letv.leui.common.recommend.widget.LeRecommendType;
import com.letv.leui.common.recommend.widget.LeRecommendViewStyle;
import com.letv.leui.common.recommend.widget.adapter.*;
import com.letv.leui.common.recommend.widget.adapter.listener.BaseItemClickListener;
import com.letv.leui.common.recommend.widget.adapter.vo.DemoAdapterItem;
import com.letv.leui.common.recommend.widget.recyclerviewmanager.MyLinearLayoutManager;

import java.util.ArrayList;


/**
 * Created by dupengtao on 14-12-3.
 */
public class LeRecommendView extends AbsLeRecommendView {

    private RecyclerView mRvList;
    private BaseRecommendAdapter baseRecommendAdapter;
    private BaseItemClickListener mBaseItemClickListener;

    public LeRecommendView(Context context) {
        super(context);
    }

    @Override
    protected int getContentLayoutResId() {
        return R.layout.view_recommend_recyclerview;
    }

    @Override
    protected void initContentView(View rootView) {
        mRvList = (RecyclerView) rootView;
        mRvList.setHasFixedSize(true);
        LinearLayoutManager llm = new MyLinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        mRvList.setItemAnimator(new DefaultItemAnimator());
        mRvList.setLayoutManager(llm);
        mRvList.setOverScrollMode(OVER_SCROLL_NEVER);
    }

    @Override
    protected void setViewData(LeRecommendType recommendType, ArrayList itemList, LeRecommendViewStyle style) {
        if (LeRecommendType.HOT_PRODUCT == recommendType) {
            baseRecommendAdapter = new RecommendHotAdapter(itemList,style);
            mBaseItemClickListener.setRecommendHotDTOs(itemList);
        } else if (LeRecommendType.LATEST_NEWS == recommendType) {
            baseRecommendAdapter = new RecommendLatestNewsAdapter(itemList,style);
            mBaseItemClickListener.setVideoList(itemList);
        } else if (LeRecommendType.WALLPAPER == recommendType) {
            baseRecommendAdapter = new RecommendWallpaperAdapter(itemList);
            mBaseItemClickListener.setWallpaperList(itemList);
        } else if (LeRecommendType.MUSIC == recommendType) {
            baseRecommendAdapter = new RecommendMusicAdapter(itemList,style);
            mBaseItemClickListener.setMusicList(itemList);
        }  else if (LeRecommendType.CALENDAR == recommendType) {
            baseRecommendAdapter = new RecommendCalendarAdapter(itemList,style);
            mBaseItemClickListener.setCalendarList(itemList);
        } else if (LeRecommendType.ALBUM == recommendType) {
            baseRecommendAdapter = new RecommendAlbumAdapter(itemList, style);
            mBaseItemClickListener.setAlbumList(itemList);
        } else if (LeRecommendType.ARTISTS == recommendType) {
            baseRecommendAdapter = new RecommendArtistsAdapter(itemList, style);
            mBaseItemClickListener.setArtistsList(itemList);
//            if (style == LeRecommendViewStyle.WHITE) {
//                mRvList.setBackgroundResource(R.color.item_recommend_card_background_color_white);
//            }
        } else {
            ArrayList<DemoAdapterItem> items = new ArrayList<DemoAdapterItem>();
            //for (int i = 0, j = Images.imageThumbUrls.length; i < j; i++) {
            //    items.add(new DemoAdapterItem("item_" + i, Images.imageThumbUrls[i]));
            //}
            baseRecommendAdapter = new DemoRecommendAdapter(items);
        }

        mRvList.setAdapter(baseRecommendAdapter);
        baseRecommendAdapter.setItemClickListener(mBaseItemClickListener);
    }

    @Override
    protected void setItemClickListener(BaseItemClickListener listener) {
        this.mBaseItemClickListener = listener;
    }

    /**
     * About RecyclerView API
     * <p/>
     * https://developer.android.com/reference/android/support/v7/widget/RecyclerView.html
     */
    public RecyclerView getRvView() {
        return mRvList;
    }

}
