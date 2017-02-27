package com.letv.wallet.online.test;

import com.letv.wallet.common.http.beans.LetvBaseBean;

/**
 * Created by changjiajie on 16-9-26.
 */
public class OrderBean implements LetvBaseBean {
    //"{\"data\":{\"order_no\":\"1610961474969164\",\"is_apart\":0,\"sub_order_no\":null,\"price\":1,\"price_by_cent\":100,\"discount_price\":0,\"discount_price_by_cent\":0},\"errno\":10000,\"errmsg\":\"\"}"
    public OrderInfo data;

    public static class OrderInfo implements LetvBaseBean {
        public String order_no;
        public OrderToken result;
    }

    public static class OrderToken implements LetvBaseBean {
        public String token;
    }
}
