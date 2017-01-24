package com.letv.walletbiz.main.recommend.bean;

import com.letv.walletbiz.main.recommend.view.RecommendCardFactory;

/**
 * Created by liuliang on 17-1-3.
 */

public class CardCBean implements BaseCardBean {

    /**
     * 图片地址
     */
    public String img;

    /**
     * 标题
     */
    public String title;

    /**
     * 内容
     */
    public String body;

    /**
     * 链接
     */
    public String link;

    /**
     * 排序，从大到小
     */
    public long rank;

    @Override
    public String getType() {
        return RecommendCardFactory.CARD_TYPE_C;
    }

    @Override
    public int compareTo(BaseCardBean o) {
        if (o instanceof CardCBean) {
            return rank >= ((CardCBean) o).rank ? -1 : 1;
        }
        return 0;
    }
}
