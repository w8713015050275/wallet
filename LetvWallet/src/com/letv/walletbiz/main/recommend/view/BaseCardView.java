package com.letv.walletbiz.main.recommend.view;

import com.letv.walletbiz.main.recommend.bean.BaseCardBean;

import java.util.HashMap;
import java.util.List;

/**
 * Created by liuliang on 17-2-6.
 */

public interface BaseCardView {

    void bindView(List<BaseCardBean> cardList);

    boolean checkContent();

    HashMap<String, String> getContentParam();

    boolean needTopDivider();

    boolean needBottomDivider();
}
