package com.letv.walletbiz.member.beans;

/**
 * Created by zhangzhiwei1 on 16-11-21.
 */

import com.letv.wallet.common.http.beans.LetvBaseBean;

public class CouponBean implements LetvBaseBean {

    public long ucoupon_id;
    public String ucoupon_code;
    public int rank_id;
    public int type;
    public String title;

    public String service_name;
    public String use_condition;
    public String use_detail_link;
    public String icon;
    public int jump_type;

    public String jump_param;
    public String package_name;
    public String jump_link;
    public long start_time;
    public long end_time;

    public int state;
    public ShowItemBean[] showItems;
    public float total_price;
    public float discount_price;
    public float real_price;

    public int getState() {
        return state;
    }

    public String getTitle() {
        return title;
    }

    public String getService_name() {
        return service_name;
    }

    public static class ShowItemBean implements LetvBaseBean {

        public String name;
        public String value;
        public int rank;
    }
}

