package com.letv.walletbiz.base.http.beans.order;

import com.letv.wallet.common.http.beans.LetvBaseBean;

/**
 * Created by changjiajie on 16-1-21.
 */
public class OrderListBaseBean<T> implements LetvBaseBean {

    /**
     * 默认最新的一条记录的id
     */
    public long last_id = 0;
    /**
     * 查询需要的条数
     */
    public int limit = 0;
    /**
     * 1：标示向后取数据，-1：标识向前取数据
     */
    public int model = 1;
    /**
     * 数据
     */
    public T[] list;

}
