package com.letv.walletbiz.movie.beans;

import com.letv.wallet.common.http.beans.LetvBaseBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by changjiajie on 16-2-1.
 */
public class MovieSoldInfo implements LetvBaseBean {

    public int mColumnNum;
    public int mRowNum;
    /**
     * 区
     */
    public String area;
    /**
     * 行号
     */
    public String desc;
    /**
     * 座位号
     */
    public List<String> seatNumList = new ArrayList<String>();
    /**
     * 选择座位号
     */
    public String n;
}
