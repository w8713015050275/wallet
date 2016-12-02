package com.letv.walletbiz.movie.beans;


import com.letv.wallet.common.http.beans.LetvBaseBean;

/**
 * Created by changjiajie on 16-2-1.
 */
public class MovieSeatList implements LetvBaseBean {

    public String roomid;
    /**
     * 座位信息
     */
    public MovieSeatRowInfo[] seat_info;
}
