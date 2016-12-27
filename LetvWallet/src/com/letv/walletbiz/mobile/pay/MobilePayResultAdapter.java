package com.letv.walletbiz.mobile.pay;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.letv.walletbiz.R;
import com.letv.walletbiz.base.activity.PayResultActivity;
import com.letv.walletbiz.mobile.widget.MobilePayResultInfoBrief;

/**
 * Created by linquan on 15-12-9.
 */
public class MobilePayResultAdapter implements PayResultActivity.PayResultAdapter {
    MobileProduct mProduct;


    public MobilePayResultAdapter(MobileProduct product) {
        mProduct = product;

    }

    @Override
    public int getTitle() {
        return R.string.movie_order_view_label;
    }

    @Override
    public int getStatus() {
        return mProduct.getPayResult();
    }

    @Override
    public String getCost() {
        return mProduct.getRealPrice();
    }

    @Override
    public View createContentView(Context context, ViewGroup parent) {
        MobilePayResultInfoBrief v = new MobilePayResultInfoBrief(context);
        v.setPayResultInfo(mProduct.getProductName(), mProduct.getSN(),mProduct.getNumber());
        return v;
    }

    private boolean isPaid() {
        return getStatus() == 1;
    }

    @Override
    public int getActionLabel() {
        return isPaid() ? R.string.label_done : R.string.pay_now;
    }

    @Override
    public void onAction(Context context) {
        ((Activity) context).finish();
    }

    @Override
    public void onBack(Activity activity) {

    }


}
