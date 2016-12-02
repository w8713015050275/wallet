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
import com.letv.leui.common.recommend.widget.adapter.dto.RecommendSiteDTO;
import com.letv.leui.common.recommend.widget.adapter.listener.BaseItemClickListener;
import com.letv.leui.common.recommend.widget.adapter.listener.RecommendImageListener;

import java.util.ArrayList;

/**
 * Created by zhangjiahao on 15-10-12.
 */
public class LeRecommendSitesView extends LeRecommendVerticalView<LeRecommendSitesView.SitesViewHolder, RecommendSiteDTO> {

    public LeRecommendSitesView(Context context, int count) {
        super(context, count);
    }

    @Override
    protected void setDataForItemClickListener(BaseItemClickListener baseItemClickListener, ArrayList<RecommendSiteDTO> itemList) {
        baseItemClickListener.setSitesList(itemList);
    }

    @Override
    protected ViewGroup createItemView(LayoutInflater inflater) {
        RelativeLayout childView = (RelativeLayout) inflater.inflate(R.layout.item_recommend_weibo, ll_rootView, false);
        RelativeLayout.LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, (int) Uiutil.dipToPixels(mContext, 60));
        childView.setLayoutParams(params);
        return childView;
    }

    @Override
    protected SitesViewHolder getViewHolder(ViewGroup itemView) {
        return new SitesViewHolder(itemView);
    }

    @Override
    protected void onBindViewHolder(SitesViewHolder holder, int i, ArrayList<RecommendSiteDTO> list, LeRecommendViewStyle style) {
        if (style == LeRecommendViewStyle.WHITE) {
            holder.tv_weibo_title.setTextColor(Color.WHITE);
//            holder.blank.setBackgroundColor(Color.BLACK);
            ll_rootView.setBackgroundResource(R.color.item_recommend_card_background_color_white);
        }
        RecommendSiteDTO siteDTO = list.get(i);
        if (holder.imageContainer != null) {
            holder.imageContainer.cancelRequest();
        }
        if (siteDTO != null) {
            holder.tv_weibo_title.setText(siteDTO.getName());
            String url = siteDTO.getPic();
            holder.imageContainer = VolleyClient.loadImage(VolleyController.getInstance(mContext), url,
                    new RecommendImageListener(holder.iv_weibo_photo, 0));
        }
    }

    public class SitesViewHolder extends LeRecommendVerticalView.BaseViewHolder {

        TextView tv_weibo_title;
        ImageView iv_weibo_photo;
        ImageLoader.ImageContainer imageContainer;
        View blank;

        public SitesViewHolder(ViewGroup viewGroup) {
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
