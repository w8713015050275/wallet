package com.letv.walletbiz.mobile.pay;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.letv.walletbiz.base.activity.ActivityConstant;
import com.letv.walletbiz.base.activity.PayResultActivity;
import com.letv.walletbiz.base.pay.Product;
import com.letv.walletbiz.coupon.CouponConstant;
import com.letv.walletbiz.mobile.MobileConstant;
import com.letv.walletbiz.mobile.activity.MobileOrderConfirmationActivity;
import com.letv.walletbiz.mobile.beans.OrderBean;
import com.letv.walletbiz.mobile.beans.OrderDetailBean;

/**
 * Created by linquan on 15-12-8.
 */
public class MobileProduct extends Product {

    private String mNumber;
    private String mSkuSN;


    public MobileProduct(int title, String sn, String name, String number, String price) {
        this(title, sn, name, number, price, System.currentTimeMillis(),
                MobileConstant.ORDER_STATUS.CREATED);
    }

    public MobileProduct(int title, OrderBean order) {
        this(title, order.order_sn, order.product_name, order.number,
                order.getPrice(), order.getOrderCTime(), order.getStatusValue());
    }

    public MobileProduct(int title, OrderDetailBean orderDetailBean) {
        this(title, orderDetailBean.order_sn, orderDetailBean.getSnapshot().getGoods_title(), orderDetailBean.number,
                orderDetailBean.getPrice(), orderDetailBean.getOrderCTime(), orderDetailBean.getStatusValue());
        mProductId = orderDetailBean.snapshot.getGoods_id();
    }

    public MobileProduct(int title, int productId, String skuSN, String name, String number, String price) {
        this(title, "", name, number, price, System.currentTimeMillis(),
                MobileConstant.ORDER_STATUS.CREATED);
        mProductId = productId;
        mSkuSN = skuSN;
    }

    private MobileProduct(int title, String sn, String name, String number, String price, long time,
                          int status) {
        super();
        mTitle = title;
        mSN = sn;
        mName = name;
        mNumber = number;
        mPrice = price;
        mTime = time;
        mStatus = status;
        initAdapter();
    }

    private void initAdapter() {
        mPayAdapter = new MobilePayAdapter(this);
        mResultAdapter = (PayResultActivity.PayResultAdapter) new MobilePayResultAdapter(this);
    }

    public void showOrderSure(Context context, Long couponID) {
        Intent intent = new Intent(context, MobileOrderConfirmationActivity.class);
        Bundle b = new Bundle();
        b.putSerializable(ActivityConstant.PAY_PARAM.PAY_PRODUCT, this);
        b.putLong(CouponConstant.EXTRA_COUPON_BEAN_ID, couponID);

        intent.putExtras(b);
        context.startActivity(intent);
    }

    public String getSkuSN() {
        return mSkuSN;
    }

    public void setSN(String SN) {
        mSN = SN;
    }

    public void setProductId(int productId) {
        mProductId = productId;
    }

    public String getNumber() {
        return mNumber;
    }

    @Override
    public void setPrepayInfo(String info) {
        super.setPrepayInfo(info);
    }

    public void showPayResult(Context context, int result) {
        super.showPayResult(context, result, null);
    }
}
