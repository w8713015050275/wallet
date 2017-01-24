package com.letv.walletbiz.main.recommend.view;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.google.gson.reflect.TypeToken;
import com.letv.walletbiz.main.recommend.bean.CardCBean;
import com.letv.walletbiz.main.recommend.bean.CardDBean;
import com.letv.walletbiz.main.recommend.bean.CardEBean;
import com.letv.walletbiz.main.recommend.bean.CardFBean;
import com.letv.walletbiz.main.recommend.bean.CardGBean;
import com.letv.walletbiz.main.recommend.bean.CardHBean;

/**
 * Created by liuliang on 17-2-6.
 */

public class RecommendCardFactory {

    public static final String CARD_TYPE_C = "C";

    public static final String CARD_TYPE_D = "D";

    public static final String CARD_TYPE_E = "E";

    public static final String CARD_TYPE_F = "F";

    public static final String CARD_TYPE_G = "G";

    public static final String CARD_TYPE_H = "H";

    public static View createCardView(Context context, String type) {
        if (context == null || TextUtils.isEmpty(type)) {
            return null;
        }
        View cardView = null;
        if (CARD_TYPE_C.equals(type)) {
            cardView = new RecommendCardCView(context);
        } else if (CARD_TYPE_D.equals(type)) {
            cardView = new RecommendCardDView(context);
        } else if (CARD_TYPE_E.equals(type)) {
            cardView = new RecommendCardEView(context);
        } else if (CARD_TYPE_F.equals(type)) {
            cardView = new RecommendCardFView(context);
        } else if (CARD_TYPE_G.equals(type)) {

        } else if (CARD_TYPE_H.equals(type)) {
            cardView = new RecommendCardHView(context);
        }
        return cardView;
    }

    public static TypeToken getTypeToken(String type) {
        if (TextUtils.isEmpty(type)) {
            return null;
        }
        TypeToken typeToken = null;
        if (CARD_TYPE_C.equals(type)) {
            typeToken = new TypeToken<CardCBean>() {};
        } else if (CARD_TYPE_D.equals(type)) {
            typeToken = new TypeToken<CardDBean>() {};
        } else if (CARD_TYPE_E.equals(type)) {
            typeToken = new TypeToken<CardEBean>() {};
        } else if (CARD_TYPE_F.equals(type)) {
            typeToken = new TypeToken<CardFBean>() {};
        } else if (CARD_TYPE_G.equals(type)) {
            typeToken = new TypeToken<CardGBean>() {};
        } else if (CARD_TYPE_H.equals(type)) {
            typeToken = new TypeToken<CardHBean>() {};
        }
        return typeToken;
    }
}
