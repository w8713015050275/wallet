package com.letv.walletbiz.member.pay;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.letv.walletbiz.base.activity.ActivityConstant;
import com.letv.walletbiz.base.activity.PayResultActivity;
import com.letv.walletbiz.base.pay.Product;
import com.letv.walletbiz.coupon.CouponConstant;
import com.letv.walletbiz.member.MemberConstant;
import com.letv.walletbiz.member.beans.OrderInfoBean;
import com.letv.walletbiz.mobile.MobileConstant;
import com.letv.walletbiz.member.activity.MemberOrderConfirmActivity;

/**
 * Created by zhanghuancheng on 16-11-16.
 */
public class MemberProduct extends Product {

    public String getType() {
        return mType;
    }

    private String mType;

    public String getProtocol_url() {
        return mProtocol_url;
    }

    private String mProtocol_url;

    public String getProgress() {
        return mProgress;
    }

    private String mProgress;
    private String mSku_no;
    private String mDuration;
    private String mDescription;

    public MemberProduct(int title, int productId, String sku_no, String name, String duration, String description, String price, String type, String protocol_url) {
        this(title, "", name, duration, description, price, System.currentTimeMillis(), MobileConstant.ORDER_STATUS.CREATED);
        mProductId = productId;
        mSku_no = sku_no;
        mType = type;
        mProtocol_url = protocol_url;
    }

    public MemberProduct(int title, OrderInfoBean orderInfoBean) {
        this(title, String.valueOf(orderInfoBean.order_sn), orderInfoBean.getSnapshot().getName(), orderInfoBean.getSnapshot().getDuration(),
                orderInfoBean.getSnapshot().getDescription(), orderInfoBean.getPrice(), orderInfoBean.getOrderCTime(), Integer.valueOf(orderInfoBean.getOrderStatus()));
        mProductId = orderInfoBean.snapshot.getGoods_id();
        mProgress = orderInfoBean.progress;
    }

    @Override
    public String toString() {
        return "MemberProduct{" +
                "mSku_no='" + mSku_no + '\'' +
                ", mProductName='" + mName + '\'' +
                ", mDuration='" + mDuration + '\'' +
                ", mDescription='" + mDescription + '\'' +
                '}';
    }

    private MemberProduct(int title, String orderSN, String name, String duration, String description, String price, long time, int status) {
        super();
        mTitle = title;
        mSN = orderSN;
        mName = name;
        mDuration = duration;
        mDescription = description;
        mPrice = price;
        mTime = time;
        mStatus = status;
        initAdapter();
    }

    public String getSku_no() {
        return mSku_no;
    }

    public String getDuration() {
        return mDuration;
    }

    public void setSN(String SN) {
        mSN = SN;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setProductId(int productId) {
        mProductId = productId;
    }

    private void initAdapter() {
        mResultAdapter = (PayResultActivity.PayResultAdapter) new MemberPayResultAdapter(this);
    }

    /**
     * start order confirm activity
     * @param context
     * @param couponID
     */
    public void showOrderSure(Context context, Long couponID) {
        Intent intent = new Intent(context, MemberOrderConfirmActivity.class);
        Bundle b = new Bundle();
        b.putSerializable(ActivityConstant.PAY_PARAM.PAY_PRODUCT, this);
        b.putLong(CouponConstant.EXTRA_COUPON_BEAN_ID, couponID);
        intent.putExtras(b);
        context.startActivity(intent);
    }

    @Override
    public void setPrepayInfo(String info) {
        super.setPrepayInfo(info);
    }

    public void showPayResult(Context context, int result) {
        super.showPayResult(context, result, null);
    }
}
