package com.letv.walletbiz.member.pay;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.letv.walletbiz.R;
import com.letv.walletbiz.base.activity.PayResultActivity;
import com.letv.walletbiz.member.widget.MemberPayResultInfoBrief;

/**
 * Created by zhanghuancheng on 16-11-21.
 */

public class MemberPayResultAdapter implements PayResultActivity.PayResultAdapter {
    MemberProduct mProduct;

    public MemberPayResultAdapter(MemberProduct product) {
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
        return mProduct.getPrice();
    }

    @Override
    public View createContentView(Context context, ViewGroup parent) {
        MemberPayResultInfoBrief v = new MemberPayResultInfoBrief(context);
        v.setPayResultInfo(mProduct.getProductName(), mProduct.getSN(), mProduct.getDuration(), mProduct.getDescription());
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
