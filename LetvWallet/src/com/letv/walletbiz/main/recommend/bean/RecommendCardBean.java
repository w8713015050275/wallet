package com.letv.walletbiz.main.recommend.bean;

import android.text.TextUtils;

import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.letv.wallet.common.http.beans.LetvBaseBean;
import com.letv.wallet.common.util.ParseHelper;
import com.letv.walletbiz.main.recommend.view.RecommendCardFactory;

import java.util.Collections;
import java.util.List;

/**
 * Created by liuliang on 17-1-3.
 */

public class RecommendCardBean implements LetvBaseBean {

    /**
     * 页眉左标题
     */
    public String header_left_title;

    /**
     * 页面左链接
     */
    public String header_left_link;

    /**
     * 页眉右标题
     */
    public String header_right_title;

    /**
     * 页眉右链接
     */
    public String header_right_link;

    /**
     * 卡片类型
     */
    public String card_type;

    /**
     * 卡片包信息 数组类型
     */
    private JsonArray card_content;

    /**
     * 右滑链接
     */
    public String slide_right;

    /**
     * 左滑链接
     */
    public String slide_left;

    /**
     * 页脚校验地址
     */
    public String footer_req_url;

    /**
     * 页脚校验请求方法
     */
    public String footer_req_url_method;


    public CardFooter[] footer;

    private List<BaseCardBean> cardList;

    private String agnesCardType;

    private boolean isUploadCardExpose = false;

    public List<BaseCardBean> getCardContent() {
        if (cardList != null) {
            return cardList;
        }
        if (card_content == null) {
            return null;
        }
        String jsonArray = card_content.toString();
        if (TextUtils.isEmpty(jsonArray) || TextUtils.isEmpty(card_type)) {
            return null;
        }
        TypeToken typeToken = RecommendCardFactory.getTypeToken(card_type);
        List<BaseCardBean> list = null;
        if (typeToken != null) {
            list = ParseHelper.parseArrayByGson(jsonArray, typeToken.getType());
        }
        if (list != null) {
            Collections.sort(list);
        }
        return list;
    }

    public void setAgnesCardType(String type) {
        agnesCardType = type;
    }

    public String getAgnesCardType() {
        return agnesCardType;
    }

    public boolean isUploadCardExpose() {
        return isUploadCardExpose;
    }

    public void setUploadCardExpose(boolean uploadCardExpose) {
        isUploadCardExpose = uploadCardExpose;
    }

    public static class CardFooter implements LetvBaseBean, Comparable<CardFooter> {

        /**
         * 按钮标题
         */
        public String key_title;

        /**
         * 按钮链接 字段为空时按钮置灰
         */
        public String key_link;

        /**
         * 排序 从大到小
         */
        public long rank;

        @Override
        public int compareTo(CardFooter o) {
            return rank >= o.rank ? -1 : 1;
        }
    }
}
