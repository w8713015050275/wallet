package com.letv.walletbiz.main.recommend.bean;

import com.letv.wallet.common.http.beans.LetvBaseBean;
import com.letv.walletbiz.main.recommend.view.RecommendCardFactory;

/**
 * Created by liuliang on 17-1-11.
 */

public class CardGBean implements BaseCardBean {

    public TextBean[] left_str;

    /**
     * 中间图片
     */
    public String middle_img;

    public TextBean[] right_str;

    /**
     * 点击链接
     */
    public String link;

    /**
     * 排序 从大到小
     */
    public long rank;

    @Override
    public String getType() {
        return RecommendCardFactory.CARD_TYPE_G;
    }

    @Override
    public int compareTo(BaseCardBean o) {
        if (o instanceof CardGBean) {
            return rank >= ((CardGBean) o).rank ? -1 : 1;
        }
        return 0;
    }

    static class TextBean implements LetvBaseBean {

        /**
         * 文本串内容
         */
        public String str;

        /**
         * 文本字体大小,//单位sp
         */
        public float str_size;

        /**
         * 颜色 格式#FFFFFFFF
         */
        public String str_color;

        /**
         * 是否加粗  1:加粗 0:不加粗
         */
        public int str_b;
    }
}
