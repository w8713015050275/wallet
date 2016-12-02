package com.letv.walletbiz.coupon.beans;

import com.letv.wallet.common.http.beans.LetvBaseBean;
import com.letv.walletbiz.movie.beans.MovieOrder;

/**
 * Created by liuliang on 16-4-19.
 */
public class CardCouponList implements LetvBaseBean {

    public MovieOrder[] list;

    /**
     * 默认最新的一条记录的id;
     * 向后取数据：上次获取的最后一条记录的order_no
     * 向前取数据：获取到的记录的最前面的一条记录order_no
     */
    public long last_id;

    /**
     * 1：标示向后取数据，-1：标识向前取数据
     */
    public int model;

    /**
     * 单次获取条数，取值范围：[1, 20]
     */
    public int limit;
    public boolean  next_more;

    public long getLast_id() {
        return last_id;
    }
}
