package com.letv.walletbiz.main.recommend.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.letv.wallet.common.util.DensityUtils;
import com.letv.walletbiz.R;
import com.letv.walletbiz.main.recommend.RecommendUtils;
import com.letv.walletbiz.main.recommend.bean.BaseCardBean;
import com.letv.walletbiz.main.recommend.bean.CardCBean;

import org.xutils.xmain;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by liuliang on 17-1-3.
 */

public class RecommendCardCView extends LinearLayout implements BaseCardView, View.OnClickListener {

    private List<CardCBean> mCardList;

    public RecommendCardCView(Context context) {
        this(context, null);
    }

    public RecommendCardCView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public RecommendCardCView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(LinearLayout.VERTICAL);
    }

    @Override
    public void onClick(View v) {
        CardCBean cardCBean = (CardCBean) v.getTag();
        if (cardCBean == null || TextUtils.isEmpty(cardCBean.link)) {
            return;
        }
        RecommendUtils.uploadCardClick(this);
        RecommendUtils.launchUrl(getContext(), cardCBean.link);
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
        if (cardList.equals(mCardList) || !(cardList.get(0) instanceof CardCBean)) {
            return;
        }
        mCardList = Arrays.asList(cardList.toArray(new CardCBean[]{}));
        removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        TextView titleView;
        TextView bodyView;
        ImageView iconView;
        CardCBean cardCBean;
        int padding = (int) DensityUtils.dip2px(10);
        for (int i=0; i<mCardList.size(); i++) {
            cardCBean = mCardList.get(i);
            LinearLayout view = (LinearLayout) inflater.inflate(R.layout.main_recommend_cardview_c, this, false);
            titleView = (TextView) view.findViewById(R.id.card_title);
            titleView.setText(cardCBean.title);
            bodyView = (TextView) view.findViewById(R.id.card_body);
            bodyView.setText(cardCBean.body);
            iconView = (ImageView) view.findViewById(R.id.card_icon);
            xmain.image().bind(iconView, cardCBean.img);
            view.setTag(cardCBean);
            if (i == (mCardList.size() - 1)) {
                view.setPadding(0, padding, 0, 0);
            } else {
                view.setPadding(0, padding, 0, padding);
            }
            view.setGravity(Gravity.CENTER_VERTICAL);
            view.setOnClickListener(this);
            addView(view, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            if (mCardList.size() > 1 && i != (mCardList.size() - 1)) {
                inflater.inflate(R.layout.divider_horizontal, this, true);
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
