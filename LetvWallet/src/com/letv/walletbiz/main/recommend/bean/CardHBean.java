package com.letv.walletbiz.main.recommend.bean;

import com.letv.walletbiz.main.recommend.view.RecommendCardFactory;

/**
 * Created by liuliang on 17-1-17.
 */

public class CardHBean implements BaseCardBean {

    /**
     * 文本hint
     */
    public String key;

    /**
     * 输入框默认值
     */
    public String value;

    @Override
    public String getType() {
        return RecommendCardFactory.CARD_TYPE_H;
    }

    @Override
    public int compareTo(BaseCardBean o) {
        return 0;
    }
}
