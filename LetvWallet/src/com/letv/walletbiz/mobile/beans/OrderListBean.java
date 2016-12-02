package com.letv.walletbiz.mobile.beans;

import com.letv.wallet.common.http.beans.LetvBaseBean;

/**
 * Created by linquan on 15-11-25.
 */
public class OrderListBean implements LetvBaseBean {


    public OrderBean[] list;
    public int total_count;


    /*{"data":{
        "order_list": [
        {"order_sn":2015112511195,"order_status":2,"order_ctime":1448425122,
        "number":13910023026,"product_name":"50\u5143\u8bdd\u8d39","price":49},
        ],     "total_count":5},"errno":10000,"errmsg":""}
*/


}
