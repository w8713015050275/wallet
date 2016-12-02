package com.letv.walletbiz.coupon.beans;

/**
 * Created by lijunying on 16-4-18.
 */
public class CouponListRequestParams {
    private long last_id;
    private int model;
    private int limit;

    public long getLast_id() {
        return last_id;
    }

    public void setLast_id(long last_id) {
        this.last_id = last_id;
    }

    public int getModel() {
        return model;
    }

    public void setModel(int model) {
        this.model = model;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
