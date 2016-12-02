package com.letv.walletbiz.movie.beans;

import com.letv.wallet.common.http.beans.LetvBaseBean;

/**
 * Created by liuliang on 16-2-16.
 */
public class LockSeatOrder implements BaseMovieOrder {

    //排期id
    public String mpid;

    //座位信息
    public String seat;

    //订单编号
    public String order_no;

    //锁座起始时间，UNIX时间戳
    public long lock_time;

    //锁座超时时间，单位：秒
    public int lock_expire_time;

    //原价
    public float original_price;

    //订单总价（折扣后的价格）
    public float price;

    //是否有“特价活动”优惠，1：有，0：无
    public int has_discount_activity;

    public static final int ORDER_HAS_DISCOUNT = 1;
    public static final int ORDER_NO_DISCOUNT = 0;

    //特价活动优惠信息
    public Discount discount_activity;

    public static class Discount implements LetvBaseBean {

        public String name ;

        public String desc;

        /**
         *  折扣的票数
         */
        public int count;

        /**
         * 每张票优惠的金额，可根据此字段和count拼出： 总优惠金额=discount_price元*count
         */
        public int discount_price;
    }

    @Override
    public String getMovieOrderNo() {
        return order_no;
    }

    @Override
    public float getMoviePrice() {
        return price;
    }

    @Override
    public String getMovieSeat() {
        return seat;
    }

    @Override
    public long getLockTime() {
        return lock_time;
    }

    @Override
    public int getLockExpireTime() {
        return lock_expire_time;
    }

    @Override
    public float getMovieOriginalPrice() {
        return original_price;
    }

    @Override
    public int hasDiscount() {
        return has_discount_activity;
    }

    @Override
    public Discount getMovieDiscount() {
        return discount_activity;
    }
}
