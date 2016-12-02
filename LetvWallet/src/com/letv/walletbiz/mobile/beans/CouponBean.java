package com.letv.walletbiz.mobile.beans;

import com.letv.walletbiz.coupon.beans.BaseCoupon;

/**
 * Created by changjiajie on 16-4-19.
 */
public class CouponBean extends BaseCoupon {

    protected float total_price;// 折扣前的价格
    protected float discount_price; // 折扣的价格
    protected float real_price; // 折扣后实际支付的价格

    public float getTotalPrice() {
        return total_price;
    }

    public float getDiscountPrice() {
        return discount_price;
    }

    public float getRealPrice() {
        return real_price;
    }
}
