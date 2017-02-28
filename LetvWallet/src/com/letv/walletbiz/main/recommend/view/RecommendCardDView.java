package com.letv.walletbiz.main.recommend.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.letv.wallet.common.util.DensityUtils;
import com.letv.walletbiz.R;
import com.letv.walletbiz.main.recommend.RecommendUtils;
import com.letv.walletbiz.main.recommend.bean.BaseCardBean;
import com.letv.walletbiz.main.recommend.bean.CardDBean;

import org.xutils.xmain;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by liuliang on 17-2-6.
 */

public class RecommendCardDView extends LinearLayout implements BaseCardView, View.OnClickListener {

    private List<CardDBean> mCardList;

    public RecommendCardDView(Context context) {
        this(context, null);
    }

    public RecommendCardDView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public RecommendCardDView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(VERTICAL);
    }

    @Override
    public void onClick(View v) {
        CardDBean cardDBean = (CardDBean) v.getTag();
        if (cardDBean == null) {
            return;
        }
        RecommendUtils.uploadCardClick(this);
        RecommendUtils.launchUrl(getContext(), cardDBean.link);
    }

    @Override
    public boolean checkContent() {
        return true;
    }

    @Override
    public HashMap<String, String> getContentParam() {
        return null;
    }

    @Override
    public void bindView(List<BaseCardBean> cardList) {
        if (cardList == null || cardList.size() <= 0) {
            return;
        }
        if (cardList.equals(mCardList) || !(cardList.get(0) instanceof CardDBean)) {
            return;
        }
        mCardList = Arrays.asList(cardList.toArray(new CardDBean[]{}));
        removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        TextView titleView;
        ImageView imageView;
        CardDBean cardDBean;
        int padding = (int) DensityUtils.dip2px(10);
        for (int i=0; i<mCardList.size(); i++) {
            cardDBean = mCardList.get(i);
            View view = inflater.inflate(R.layout.main_recommend_cardview_d, this, false);
            titleView = (TextView) view.findViewById(R.id.content_title);
            if (TextUtils.isEmpty(cardDBean.title)) {
                titleView.setVisibility(View.GONE);
            } else {
                titleView.setText(cardDBean.title);
                titleView.setVisibility(View.VISIBLE);
            }

            imageView = (ImageView) view.findViewById(R.id.content_img);
            if (TextUtils.isEmpty(cardDBean.img)) {
                imageView.setVisibility(View.GONE);
            } else {
                imageView.setVisibility(View.VISIBLE);
                xmain.image().bind(imageView, cardDBean.img);
            }
            if (!TextUtils.isEmpty(cardDBean.title) && !TextUtils.isEmpty(cardDBean.img)) {
                titleView.setPadding(0, 0, 0, padding);
            } else {
                titleView.setPadding(0, 0, 0, 0);
            }

            view.setTag(cardDBean);
            view.setPadding(padding, padding, padding, padding);
            view.setOnClickListener(this);
            addView(view, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            if (mCardList.size() > 1 && i != (mCardList.size() - 1)) {
                inflater.inflate(R.layout.recommend_divider_horizontal, this, true);
            }
        }
    }

    @Override
    public boolean needTopDivider() {
        return true;
    }

    @Override
    public boolean needBottomDivider() {
        return true;
    }
}
