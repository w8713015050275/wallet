package com.letv.leui.common.recommend.widget.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.letv.leui.common.recommend.utils.Uiutil;
import com.letv.leui.common.recommend.widget.adapter.listener.OnBaseItemClickListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dupengtao on 14-11-12.
 */
public abstract class BaseRecommendAdapter<T, K extends BaseRecommendAdapter.BaseRecommendViewHolder> extends RecyclerView.Adapter<K> {
    public static final int MAX_ITEM_NUM = 16;
    protected Context mContext;
    private int screenW;
    protected List<T> mDataSet;
    OnBaseItemClickListener itemClickListener;

    private int ivPhotoId, tvSongNameId, tvAlbumId;
    protected boolean mHasMoreItem;


    public BaseRecommendAdapter(List<T> dataSet) {
        mDataSet = dataSet == null ? new ArrayList<T>() : dataSet;

    }

    @Override
    public final K onCreateViewHolder(ViewGroup viewGroup, int i) {

        if (mContext == null) {
            mContext = viewGroup.getContext();
            DisplayMetrics dm = new DisplayMetrics();
            ((Activity) mContext).getWindowManager().getDefaultDisplay()
                    .getMetrics(dm);
            screenW = dm.widthPixels;
        }
        RelativeLayout itemView = (RelativeLayout) LayoutInflater.from(mContext).inflate(getItemLayoutResId(), viewGroup, false);

        //RelativeLayout itemView =initView();

        ViewGroup parent = (ViewGroup) viewGroup.getParent().getParent();
        int parentPaddingLeft = parent.getPaddingLeft();
        int parentPaddingRight = parent.getPaddingRight();
        RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) itemView.getLayoutParams();
        int end = layoutParams.getMarginEnd();
        int start = layoutParams.getMarginStart();

        //int singleW = screenW
        float screenItemCount = getScreenItemCount(screenW, parentPaddingLeft, parentPaddingRight, end, start);
        if (screenItemCount > 0) {
            layoutParams.width = (int) (screenW / screenItemCount) - end - start - parentPaddingLeft - parentPaddingRight;
        }

        return getViewHolder(itemView);
    }

    protected abstract int getItemLayoutResId();

    abstract K getViewHolder(RelativeLayout itemView);

    abstract float getScreenItemCount(int screenW, int parentPaddingLeft, int parentPaddingRight, int end, int start);

    private RelativeLayout initView() {

        int dp_5 = (int) Uiutil.dipToPixels(mContext, 5);
        int dp_4 = (int) Uiutil.dipToPixels(mContext, 4);

        RelativeLayout itemBox = new RelativeLayout(mContext);
        itemBox.setId(View.generateViewId());
        RelativeLayout.LayoutParams itemBoxLP = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        itemBoxLP.setMargins(dp_5, dp_5, dp_5, dp_5);
        itemBox.setGravity(Gravity.CENTER);
        itemBox.setLayoutParams(itemBoxLP);

        RelativeLayout rlPhotoBox = new RelativeLayout(mContext);
        rlPhotoBox.setId(View.generateViewId());
        RelativeLayout.LayoutParams rlPhotoBoxLP = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rlPhotoBox.setLayoutParams(rlPhotoBoxLP);
        itemBox.addView(rlPhotoBox);

        ImageView ivPhoto = new ImageView(mContext);
        ivPhoto.setId(View.generateViewId());
        RelativeLayout.LayoutParams ivPhotoLP = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        ivPhotoLP.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        ivPhoto.setLayoutParams(ivPhotoLP);
        rlPhotoBox.addView(ivPhoto);
        ivPhotoId = ivPhoto.getId();

        TextView tvSongName = new TextView(mContext);
        tvSongName.setId(View.generateViewId());
        RelativeLayout.LayoutParams tvSongNameLP = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        tvSongNameLP.addRule(RelativeLayout.BELOW, rlPhotoBox.getId());
        tvSongNameLP.setMargins(0, dp_5, 0, 0);
        tvSongName.setGravity(Gravity.CENTER);
        tvSongName.setTextSize(24);
        tvSongName.setTextColor(Color.parseColor("#212121"));
        tvSongName.setClickable(false);
        tvSongName.setLayoutParams(tvSongNameLP);
        itemBox.addView(tvSongName);
        tvSongNameId = tvSongName.getId();

        TextView tvAlbumName = new TextView(mContext);
        tvAlbumName.setId(View.generateViewId());
        RelativeLayout.LayoutParams tvAlbumNameLP = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        tvAlbumNameLP.addRule(RelativeLayout.BELOW, tvSongName.getId());
        tvAlbumNameLP.setMargins(0, dp_5, 0, 0);
        tvAlbumName.setGravity(Gravity.CENTER);
        tvAlbumName.setTextSize(24);
        tvAlbumName.setTextColor(Color.parseColor("#212121"));
        tvAlbumName.setClickable(false);
        tvAlbumName.setLayoutParams(tvAlbumNameLP);
        itemBox.addView(tvAlbumName);
        tvAlbumId = tvAlbumName.getId();

        return itemBox;
    }

    //@Override
    //public void onBindViewHolder(BaseRecommendViewHolder baseRecommendViewHolder, int i) {
    //    DemoAdapterItem item = mDataSet.get(i);
    //    baseRecommendViewHolder.setSongName(item.getItemName());
    //    baseRecommendViewHolder.setAlbumName(item.getItemName());
    //    if(baseRecommendViewHolder.imageContainer!=null){
    //        baseRecommendViewHolder.imageContainer.cancelRequest();
    //    }
    //    baseRecommendViewHolder.imageContainer= VolleyClient.loadImage(VolleyController.getInstance(mContext), item.getItemUrl(), baseRecommendViewHolder.ivPhoto, R.drawable.ic_launcher, R.drawable.ic_launcher);
    //}

    @Override
    public int getItemCount() {
        int size = mDataSet.size();
        mHasMoreItem = size >= MAX_ITEM_NUM;
        int dataSize = mHasMoreItem ? 16 : size;
        return dataSize;
    }


    public abstract class BaseRecommendViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        RelativeLayout mItemView;
        //TextView tvSongName,tvAlbumName;
        //ImageLoader.ImageContainer imageContainer;

        public BaseRecommendViewHolder(RelativeLayout itemView) {
            super(itemView);
            mItemView = itemView;
            initItemView(mItemView);
            setListener();
            //tvSongName = (TextView) mItemView.findViewById(tvSongNameId);
            //tvAlbumName = (TextView) mItemView.findViewById(tvAlbumId);
            //ivPhoto = (ImageView) mItemView.findViewById(ivPhotoId);
            //tvSongName = (TextView) mItemView.findViewById(R.id.tv_song_name);
            //tvAlbumName = (TextView) mItemView.findViewById(R.id.tv_album_name);
            //ivPhoto = (ImageView) mItemView.findViewById(R.id.iv_photo);
        }

        protected void setListener() {
            mItemView.setOnClickListener(this);
        }

        protected abstract void initItemView(RelativeLayout mItemView);

        @Override
        public void onClick(View v) {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(v, getPosition(), getItemCount());
            }
        }

        //public void setSongName(String text){
        //    tvSongName.setText(text);
        //}
        //
        //public void setAlbumName(String text){
        //    tvAlbumName.setText(text);
        //}

    }

    public OnBaseItemClickListener getItemClickListener() {
        return itemClickListener;
    }

    public void setItemClickListener(OnBaseItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }
}
