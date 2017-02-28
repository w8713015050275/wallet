package com.letv.walletbiz.main.recommend.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.letv.wallet.common.util.DensityUtils;
import com.letv.walletbiz.R;
import com.letv.walletbiz.main.recommend.bean.BaseCardBean;
import com.letv.walletbiz.main.recommend.bean.CardHBean;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by liuliang on 17-2-7.
 */

public class RecommendCardHView extends LinearLayout implements BaseCardView {

    private List<CardHBean> mCardList;

    public RecommendCardHView(Context context) {
        this(context, null);
    }

    public RecommendCardHView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public RecommendCardHView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(HORIZONTAL);
    }

    @Override
    public void bindView(List<BaseCardBean> cardList) {
        if (cardList == null || cardList.size() <= 0) {
            return;
        }
        if (cardList.equals(mCardList) || !(cardList.get(0) instanceof CardHBean)) {
            return;
        }
        mCardList = Arrays.asList(cardList.toArray(new CardHBean[]{}));
        removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        if (inflater == null) {
            return;
        }
        int padding = (int) DensityUtils.dip2px(10);
        CardHBean cardBean;
        TextView titleView;
        TextView summaryView;
        for (int i=0; i<mCardList.size(); i++) {
            cardBean = mCardList.get(i);
            View view = inflater.inflate(R.layout.main_recommend_cardview_h, this, false);
            titleView = (TextView) view.findViewById(R.id.content_title);
            titleView.setText(cardBean.key + ":");
            summaryView = (TextView) view.findViewById(R.id.content_summary);
            summaryView.setText(cardBean.value);
            view.setPadding(padding, padding, padding, padding);
            addView(view, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            if (mCardList.size() > 1 && i != (mCardList.size() - 1)) {
                inflater.inflate(R.layout.recommend_divider_horizontal, this, true);
            }
        }

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
    public boolean needTopDivider() {
        return true;
    }

    @Override
    public boolean needBottomDivider() {
        return true;
    }
}
