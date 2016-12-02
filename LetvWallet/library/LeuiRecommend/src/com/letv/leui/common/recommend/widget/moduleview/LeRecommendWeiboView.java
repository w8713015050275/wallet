package com.letv.leui.common.recommend.widget.moduleview;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.letv.leui.common.R;
import com.letv.leui.common.recommend.net.VolleyClient;
import com.letv.leui.common.recommend.net.VolleyController;
import com.letv.leui.common.recommend.utils.Uiutil;
import com.letv.leui.common.recommend.volley.toolbox.ImageLoader;
import com.letv.leui.common.recommend.widget.LeRecommendViewStyle;
import com.letv.leui.common.recommend.widget.adapter.dto.RecommendTweetDTO;
import com.letv.leui.common.recommend.widget.adapter.listener.BaseItemClickListener;
import com.letv.leui.common.recommend.widget.adapter.listener.RecommendImageListener;

import java.util.ArrayList;

/**
 * Created by zhangjiahao on 15-7-9.
 */
public class LeRecommendWeiboView extends LeRecommendVerticalView<LeRecommendWeiboView.WeiBoViewHolder, RecommendTweetDTO> {

    public LeRecommendWeiboView(Context context, int count) {
        super(context, count);
    }

    @Override
    protected ViewGroup createItemView(LayoutInflater inflater) {
        RelativeLayout childView = (RelativeLayout) inflater.inflate(R.layout.item_recommend_weibo, ll_rootView, false);
        RelativeLayout.LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, (int) Uiutil.dipToPixels(mContext, 60));
        childView.setLayoutParams(params);
        return childView;
    }

    @Override
    protected void setDataForItemClickListener(BaseItemClickListener baseItemClickListener, ArrayList<RecommendTweetDTO> itemList) {
        baseItemClickListener.setTweetData(itemList);
    }

    @Override
    protected WeiBoViewHolder getViewHolder(ViewGroup itemView) {
        return new WeiBoViewHolder(itemView);
    }

    @Override
    protected void onBindViewHolder(WeiBoViewHolder holder, int i, ArrayList<RecommendTweetDTO> list, LeRecommendViewStyle style) {
        if (style == LeRecommendViewStyle.WHITE) {
            holder.tv_weibo_title.setTextColor(Color.WHITE);
//            holder.blank.setBackgroundColor(Color.BLACK);
//            ll_rootView.setBackgroundResource(R.color.item_recommend_card_background_color_white);
        }
        RecommendTweetDTO weiBoDTO = list.get(i);
        if (holder.imageContainer != null) {
            holder.imageContainer.cancelRequest();
        }
        RecommendTweetDTO.RecommendWeiboStarInfo starinfo = weiBoDTO.getStarinfo();
        if (starinfo != null) {
            holder.tv_weibo_title.setText(weiBoDTO.getStarinfo().getScreen_name());
            String url = weiBoDTO.getStarinfo().getAvatar_large();
            holder.imageContainer = VolleyClient.loadImage(VolleyController.getInstance(mContext), url,
                    new RecommendImageListener(holder.iv_weibo_photo, 0));
        }
    }

    public class WeiBoViewHolder extends LeRecommendVerticalView.BaseViewHolder{

        TextView tv_weibo_title;
        ImageView iv_weibo_photo;
        ImageLoader.ImageContainer imageContainer;
        View blank;

        public WeiBoViewHolder(ViewGroup viewGroup) {
            super(viewGroup);
        }

        @Override
        protected void initViewHolder(ViewGroup viewGroup) {
            this.iv_weibo_photo = (ImageView) viewGroup.findViewById(R.id.iv_weibo_photo);
            this.tv_weibo_title = (TextView) viewGroup.findViewById(R.id.tv_weibo_title);
//            this.blank = viewGroup.findViewById(R.id.blank);
        }

    }
}
