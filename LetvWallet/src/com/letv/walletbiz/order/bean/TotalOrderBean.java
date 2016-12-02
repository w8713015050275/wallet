package com.letv.walletbiz.order.bean;

import android.content.Context;

import com.letv.wallet.common.util.DateUtils;
import com.letv.walletbiz.R;
import com.letv.walletbiz.base.http.beans.order.OrderBaseBean;
import com.letv.walletbiz.mobile.MobileConstant;

import java.util.Date;

/**
 * Created by linquan on 15-12-8.
 */
public class TotalOrderBean implements OrderBaseBean {

    public static final int STATUS_UNKNOWN = 0;
    public static final int STATUS_UNPAY = 1;
    public static final int STATUS_PAID = 2;
    public static final int STATUS_DELIVERED = 3;
    public static final int STATUS_COMPLETED = 4;
    public static final int STATUS_CANCELED = 5;

    public long rank_id = 0;
    public String order_no;
    public int type;// 1话费充值,4流量充值,5电影票
    public String goods_title;
    public float price;
    public int progress;//订单状态
    public String progress_name;//订单状态名称
    public long order_time;//时间戳格式
    public String icon;//类型图标
    public String jump_param;

    public String toString() {
        return "order_no: " + order_no +
                "\ntype: " + type +
                "\ngoods_title: " + goods_title +
                "\nprice: " + price +
                "\nprogress: " + progress +
                "\nprogress_name: " + progress_name +
                "\norder_time: " + order_time +
                "\nicon: " + icon +
                "\njump_param: " + jump_param;

    }

    public int getOrderStatus() {
        int status = STATUS_UNKNOWN;
        switch (progress) {
            case 2:
                status = STATUS_UNPAY;
                break;
            case 5:
                status = STATUS_PAID;
                break;
            case 10:
                status = STATUS_DELIVERED;
                break;
            case 15:
                status = STATUS_COMPLETED;
                break;
            case 1:
                status = STATUS_CANCELED;
                break;
        }
        return status;
    }

    public String getOrderStatus(Context context) {
        return context.getString(getOrderStatusResId(getOrderStatus()));
    }

    public int getOrderStatusResId(int status) {
        int result = R.string.total_order_status_unknown;
        switch (status) {
            case STATUS_UNPAY:
                result = R.string.total_order_status_unpay;
                break;
            case STATUS_PAID:
                result = R.string.total_order_status_paid;
                break;
            case STATUS_DELIVERED:
                result = R.string.total_order_status_delivered;
                break;
            case STATUS_COMPLETED:
                result = R.string.total_order_status_completed;
                break;
            case STATUS_CANCELED:
                result = R.string.total_order_status_canceled;
                break;
        }
        return result;
    }

    public String getOrderNO(Context context) {
        return String.format(context.getString(R.string.total_order_no_sn), String.valueOf(order_no));
    }

    @Override
    public long getOrderCTime() {
        return order_time * 1000;
    }

    @Override
    public String getOrderId() {
        return order_no;
    }

    @Override
    public long getRankId() {
        return rank_id;
    }

    public String getPrice() {
        return String.format(MobileConstant.DPRODUCT.FORMAT_PRICE, price);
    }

    public String getWeek(Context context) {
        try {
            Date date = new Date(order_time * 1000);
            return DateUtils.formatDate(date, context.getString(R.string.total_order_list_time_week));
        } catch (Exception e) {
            return "";
        }
    }

    public int getStatusValue() {
        return progress;
    }
}
