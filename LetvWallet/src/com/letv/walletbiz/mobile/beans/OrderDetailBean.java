package com.letv.walletbiz.mobile.beans;

import android.content.Context;

import com.letv.walletbiz.base.http.beans.order.OrderBaseBean;
import com.letv.walletbiz.mobile.MobileConstant;
import com.letv.walletbiz.mobile.util.UiUtils;

/**
 * Created by changjiajie on 16-4-11.
 */
public class OrderDetailBean implements OrderBaseBean {

    public String order_sn;
    public int order_status;
    public int refund_progress;
    public long order_ctime;
    public float price;
    public float discount_price;
    public float real_price;
    public String number;
    public OrderSnapshot snapshot;

    public String toString() {
        return "order_sn: " + order_sn +
                "\norder_status: " + order_status +
                "\norder_ctime: " + order_ctime +
                "\nprice: " + price +
                "\nnumber: " + number +
                "\nsnapshot: " + (snapshot == null ? "null" : snapshot.toString());
    }

    public OrderSnapshot getSnapshot() {
        return snapshot;
    }

    public String getOrderStatus(Context context) {
        return UiUtils.getOrderStatusStringbyValue(context, order_status);
    }

    public boolean showRefundProgress() {
        boolean showRefundProgess = false;
        switch (refund_progress) {
            case MobileConstant.REFUND_PROGRESS.REFUNDING:
            case MobileConstant.REFUND_PROGRESS.REFUND_SUCCESS:
            case MobileConstant.REFUND_PROGRESS.REFUND_FAIL:
            case MobileConstant.REFUND_PROGRESS.AUDIT_FAIL:
                showRefundProgess = true;
                break;
        }
        return showRefundProgess;
    }

    public String getRefundProgressStr(Context context) {
        int index = MobileConstant.REFUND_PROGRESS_ID.REFUNDING;
        switch (refund_progress) {
            case MobileConstant.REFUND_PROGRESS.REFUNDING:
                index = MobileConstant.REFUND_PROGRESS_ID.REFUNDING;
                break;
            case MobileConstant.REFUND_PROGRESS.REFUND_SUCCESS:
                index = MobileConstant.REFUND_PROGRESS_ID.REFUND_SUCCESS;
                break;
            case MobileConstant.REFUND_PROGRESS.REFUND_FAIL:
                index = MobileConstant.REFUND_PROGRESS_ID.REFUND_FAIL;
                break;
            case MobileConstant.REFUND_PROGRESS.AUDIT_FAIL:
                index = MobileConstant.REFUND_PROGRESS_ID.AUDIT_FAIL;
                break;
        }
        return UiUtils.getRefundProgressStringbyValue(context, index);
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
        return 0;
    }

    public float getTotalPrice() {
        return price;
    }

    public float getRealPrice() {
        return real_price;
    }

    public float getDiscountPrice() {
        return discount_price;
    }

    public String getPrice() {
        return String.format(MobileConstant.DPRODUCT.FORMAT_PRICE, price);
    }

    public int getStatusValue() {
        return order_status;
    }
}
