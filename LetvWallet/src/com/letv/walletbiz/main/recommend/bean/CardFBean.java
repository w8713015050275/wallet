package com.letv.walletbiz.main.recommend.bean;

import com.letv.wallet.common.http.beans.LetvBaseBean;
import com.letv.walletbiz.main.recommend.view.RecommendCardFactory;

/**
 * Created by liuliang on 17-1-9.
 */

public class CardFBean implements BaseCardBean {

    /**
     * 内部标题
     */
    public String title;

    public ImageBean[] imgs;

    /**
     * 排序 从大到小
     */
    public long rank;

    @Override
    public String getType() {
        return RecommendCardFactory.CARD_TYPE_F;
    }

    @Override
    public int compareTo(BaseCardBean o) {
        if (o instanceof CardFBean) {
            return rank >= ((CardFBean) o).rank ? -1 : 1;
        }
        return 0;
    }

    public static class ImageBean implements LetvBaseBean {

        /**
         * 图片地址
         */
        public String img_url;

        /**
         * 图片描述
         */
        public String img_desc;

        /**
         * 图片链接
         */
        public String img_link;
    }
}
