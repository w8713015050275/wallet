package com.letv.walletbiz.mobile.beans;

import android.content.Context;

import com.letv.walletbiz.base.http.beans.order.OrderBaseBean;
import com.letv.walletbiz.mobile.MobileConstant;
import com.letv.walletbiz.mobile.util.UiUtils;

/**
 * Created by linquan on 15-12-8.
 */
public class OrderBean implements OrderBaseBean {

    public long rank_id = 0;
    public String order_sn;
    public int order_status;
    public long order_ctime;
    public String number;
    public String product_name;
    public float price;

    public String toString() {
        return "order_sn: " + order_sn +
                "\norder_status: " + order_status +
                "\norder_ctime: " + order_ctime +
                "\nnumber: " + number +
                "\nproduct_name: " + product_name +
                "\nprice: " + price;

    }

    public String getOrderStatus(Context context) {
        return UiUtils.getOrderStatusStringbyValue(context, order_status);
    }

    public int getOrderTodo() {
        if (order_status <= 0 || order_status >= MobileConstant.ORDER_STATUS.MAX)
            return MobileConstant.ORDER_TODO.NONE;

        int todo[] = {
                MobileConstant.ORDER_TODO.TOPAY,
                MobileConstant.ORDER_TODO.TOPAY,
                MobileConstant.ORDER_TODO.NONE,
                MobileConstant.ORDER_TODO.NONE,
                MobileConstant.ORDER_TODO.NONE,
                MobileConstant.ORDER_TODO.NONE};

        return todo[order_status - 1];
    }

    public String getOrderSN() {
        return order_sn;
    }

    @Override
    public long getOrderCTime() {
        return order_ctime * 1000;
    }

    @Override
    public String getOrderId() {
        return order_sn;
    }

    @Override
    public long getRankId() {
        return rank_id;
    }

    public String getPrice() {
        return String.format(MobileConstant.DPRODUCT.FORMAT_PRICE, price);
    }

    public int getStatusValue() {
        return order_status;
    }
}
