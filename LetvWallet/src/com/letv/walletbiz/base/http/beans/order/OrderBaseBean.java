package com.letv.walletbiz.base.http.beans.order;

import java.io.Serializable;

/**
 * Created by changjiajie on 16-1-21.
 */
public interface OrderBaseBean extends Serializable {

    /**
     * 获取订单时间
     */
    long getOrderCTime();

    /**
     * 获取订单id
     */
    String getOrderId();

    /**
     * 获取最后一条id
     */
    long getRankId();

    /**
     * 获取价格
     */
    String getPrice();

}
