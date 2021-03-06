package com.letv.walletbiz.mobile.pay;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.letv.walletbiz.R;
import com.letv.walletbiz.base.activity.ActivityConstant;
import com.letv.walletbiz.base.activity.PayResultActivity;
import com.letv.walletbiz.base.pay.Constants;
import com.letv.walletbiz.base.pay.Product;
import com.letv.walletbiz.base.util.WalletConstant;
import com.letv.walletbiz.coupon.CouponConstant;
import com.letv.walletbiz.mobile.MobileConstant;
import com.letv.walletbiz.mobile.activity.MobileOrderConfirmationActivity;
import com.letv.walletbiz.mobile.activity.MobileOrderDetailActivity;
import com.letv.walletbiz.mobile.beans.OrderBean;
import com.letv.walletbiz.mobile.beans.OrderDetailBean;
import com.letv.walletbiz.mobile.provider.MobileContact;

/**
 * Created by linquan on 15-12-8.
 */
public class MobileProduct extends Product {

    private String mNumber;
    private String mSkuSN;


    public MobileProduct(String sn, String name, String number, String price) {
        this(sn, name, number, price, System.currentTimeMillis(),
                MobileConstant.ORDER_STATUS.CREATED);
    }

    public MobileProduct(OrderBean order) {
        this(order.order_sn, order.product_name, order.number,
                order.getPrice(), order.getOrderCTime(), order.getStatusValue());
    }

    public MobileProduct(OrderDetailBean orderDetailBean) {
        this(orderDetailBean.order_sn, orderDetailBean.getSnapshot().getGoods_title(), orderDetailBean.number,
                orderDetailBean.getPrice(), orderDetailBean.getOrderCTime(), orderDetailBean.getStatusValue());
        mProductId = orderDetailBean.getSnapshot().getGoods_id();
    }

    public MobileProduct(int productId, String skuSN, String name, String number, String price) {
        this("", name, number, price, System.currentTimeMillis(),
                MobileConstant.ORDER_STATUS.CREATED);
        mProductId = productId;
        mSkuSN = skuSN;
    }

    private MobileProduct(String sn, String name, String number, String price, long time,
                          int status) {
        super();
        mTitle = R.string.mobile_order_view_label;
        mSN = sn;
        mName = name;
        mNumber = number;
        mPrice = price;
        mRealPrice = price;
        mTime = time;
        mStatus = status;
        initAdapter();
    }

    private void initAdapter() {
        mPayAdapter = new MobilePayAdapter(this);
        mResultAdapter = (PayResultActivity.PayResultAdapter) new MobilePayResultAdapter(this);
    }

    public void showOrderSure(Context context, String from, int feeOrFlow, Long couponID, int contactsType) {
        Intent intent = new Intent(context, MobileOrderConfirmationActivity.class);
        Bundle b = new Bundle();
        b.putSerializable(ActivityConstant.PAY_PARAM.PAY_PRODUCT, this);
        // 表示流量话费页面的from
        b.putString(WalletConstant.EXTRA_FROM, from);
        b.putLong(CouponConstant.EXTRA_COUPON_BEAN_ID, couponID);
        b.putInt(MobileConstant.PARAM.FEEFLOW_KEY, feeOrFlow);
        b.putInt(MobileConstant.PARAM.CONTACT_TYPE_KEY, contactsType);
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
