package com.letv.walletbiz.member.beans;

/**
 * Created by zhangzhiwei1 on 16-11-21.
 */

import com.letv.wallet.common.http.beans.LetvBaseBean;
import com.letv.walletbiz.base.http.beans.order.OrderBaseBean;

public class OrderInfoBean implements OrderBaseBean {

    public String order_sn;
    public String order_status;
    public String progress;
    public String order_ctime;
    public String price;
    public String discount_price;
    public String real_price;
    public Snapshot snapshot;
    public CouponItem[] coupon;

    public String getDiscount_price() {
        return discount_price;
    }

    @Override
    public long getOrderCTime() {
        return Long.valueOf(order_ctime);
    }

    public String getReal_price() {
        return real_price;
    }

    @Override
    public String getOrderId() {
        return String.valueOf(order_sn);
    }

    @Override
    public long getRankId() {
        return 0;
    }

    @Override
    public String getPrice() {
        return String.valueOf(price);
    }

    public String getOrderStatus() {
        return order_status;
    }

    public Snapshot getSnapshot() {
        return snapshot;
    }

    public static class Snapshot implements LetvBaseBean {

        public String name;
        public String merchant_id;
        public String goods_type;
        public String goods_id;
        public String sku_id;
        public String sku_no;
        public String num;
        public String goods_from;
        private String duration;
        private String description;

        public int getGoods_id() {
            return Integer.valueOf(goods_id);
        }
        public String getName() {
            return name;
        }

        public String getDuration() {
            return duration;
        }

        public String getDescription() {
            return description;
        }
    }

    public static class CouponItem implements LetvBaseBean {

        public String ucoupon_id;
        public String coupon_title;
        public String price;
    }
}
