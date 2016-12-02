package com.letv.walletbiz.mobile.beans;

import com.letv.wallet.common.http.beans.LetvBaseBean;
import com.letv.walletbiz.coupon.beans.BaseCoupon;

/**
 * Created by changjiajie on 16-4-19.
 */
public class CouponListBean<T extends BaseCoupon> implements LetvBaseBean {

    public int num;

    public T[] list;
}
