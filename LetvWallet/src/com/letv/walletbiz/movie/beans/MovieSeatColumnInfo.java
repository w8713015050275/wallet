package com.letv.walletbiz.movie.beans;

import com.letv.wallet.common.http.beans.LetvBaseBean;

/**
 * Created by changjiajie on 16-2-4.
 */
public class MovieSeatColumnInfo implements LetvBaseBean {


    public static final int DEFAULTSTATE = 0;
    public static final int SOLDSEATSTATE = 1;
    public static final int CHECKEDSTATE = 2;
    public static final String AISLE_IDENTIFY = "Z";
    public static final String USABLE_IDENTIFY = "N";
    public static final String LOVER_IDENTIFY0 = "0";
    public static final String LOVER_IDENTIFY1 = "1";
    public static final String LOVER_IDENTIFY2 = "2";

    /**
     * 是否损坏（N为可用，未损坏；n为Z时，该值为空字符串）
     */
    public String damagedFlg;

    /**
     * 是否情侣座（1,2是，0否；n为Z时，该值为空字符串）
     */
    public String loveInd;
    /**
     * 座位竖列号（Z为走道）
     */
    public String n;

    public int state = DEFAULTSTATE;

    public int getN() {
        return n != null ? Integer.valueOf(n) : 0;
    }
}
