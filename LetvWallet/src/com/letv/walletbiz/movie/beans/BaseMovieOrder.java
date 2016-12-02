package com.letv.walletbiz.movie.beans;

import com.letv.wallet.common.http.beans.LetvBaseBean;

/**
 * Created by liuliang on 16-3-2.
 */
public interface BaseMovieOrder extends LetvBaseBean {

    String getMovieOrderNo();

    float getMoviePrice();

    String getMovieSeat();

    long getLockTime();

    int getLockExpireTime();

    float getMovieOriginalPrice();

    int hasDiscount();

    LockSeatOrder.Discount getMovieDiscount();
}
