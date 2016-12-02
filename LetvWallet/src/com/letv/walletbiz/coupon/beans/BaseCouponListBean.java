package com.letv.walletbiz.coupon.beans;

import com.letv.wallet.common.http.beans.LetvBaseBean;

/**
 * Created by lijunying on 16-4-18.
 */
public class BaseCouponListBean<T extends BaseCoupon> implements LetvBaseBean {
    public long last_id;

    public int model;
    
    public int limit;

    public T[] list;

    public boolean next_more;

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

    public void setList(T[] list) {
        this.list = list;
    }

    public T[] getList() {
        return list;
    }
}
