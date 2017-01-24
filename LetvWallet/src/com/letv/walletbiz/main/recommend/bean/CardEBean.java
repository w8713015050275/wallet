package com.letv.walletbiz.main.recommend.bean;

import android.text.TextUtils;

import com.letv.walletbiz.main.recommend.view.RecommendCardFactory;

import java.util.regex.Pattern;

/**
 * Created by liuliang on 17-1-9.
 */

public class CardEBean implements BaseCardBean {

    /**
     * 普通文本
     */
    public static final int I_TYPE_TEXT = 1;
    /**
     * 数字字符串
     */
    public static final int I_TYPE_NUMBER = 2;
    /**
     * 城市列表
     */
    public static final int I_TYPE_CITY = 3;
    /**
     * 时间--年月日
     */
    public static final int I_TYPE_DATE = 4;

    /**
     * 输入提示
     */
    public String i_hint;

    /**
     * 输入参数名
     */
    public String i_name;

    /**
     * 输入类型，文本，地里位置，时间
     */
    public int i_type;

    /**
     * 输入正则校验
     */
    private String i_regular;

    /**
     * 排序 从大到小
     */
    public long rank;

    private Pattern mPattern;

    @Override
    public String getType() {
        return RecommendCardFactory.CARD_TYPE_E;
    }

    public boolean matcher(String text) {
        if (TextUtils.isEmpty(i_regular)) {
            return true;
        }
        if (mPattern == null) {
            mPattern = Pattern.compile(i_regular);
        }
        return mPattern.matcher(text).matches();
    }

    @Override
    public int compareTo(BaseCardBean o) {
        if (o instanceof CardEBean) {
            return rank >= ((CardEBean) o).rank ? -1 : 1;
        }
        return 0;
    }
}
