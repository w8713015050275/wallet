package com.letv.walletbiz.movie.beans;

import com.letv.wallet.common.http.beans.LetvBaseBean;

/**
 * Created by changjiajie on 16-2-1.
 */
public class MovieSeatRowInfo implements LetvBaseBean {
    /**
     * 座位区域编号
     */
    public String area;
    /**
     * 座位横排坐标
     */
    public int row;
    /**
     * 座位横排号
     */
    public String desc;
    /**
     * 座位横排中每一个座位的信息数组
     */
    public MovieSeatColumnInfo[] detail;
}
