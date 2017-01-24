package com.letv.walletbiz.main.recommend.bean;

import com.letv.walletbiz.main.recommend.view.RecommendCardFactory;

/**
 * Created by liuliang on 17-1-9.
 */

public class CardDBean implements BaseCardBean {

    /**
     * 图片地址
     */
    public String img;

    /**
     * 内部标题
     */
    public String title;

    /**
     * 内部链接
     */
    public String link;

    /**
     * 排序  从大到小
     */
    public long rank;

    @Override
    public String getType() {
        return RecommendCardFactory.CARD_TYPE_D;
    }

    @Override
    public int compareTo(BaseCardBean o) {
        if (o instanceof CardDBean) {
            return rank >= ((CardDBean) o).rank ? -1 : 1;
        }
        return 0;
    }
}
