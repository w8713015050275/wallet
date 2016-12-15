package com.letv.walletbiz.member.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.letv.walletbiz.R;

/**
 * Created by zhanghuancheng on 16-11-24.
 */
public class MemberPayResultInfoBrief extends LinearLayout {

    private Context mContext;
    private TextView tvProductName;
    private TextView tvSn;
    private TextView tvDuration;
    private TextView tvDescription;

    public MemberPayResultInfoBrief(Context context) {
        this(context, null);
    }

    public MemberPayResultInfoBrief(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MemberPayResultInfoBrief(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public void init(Context context, AttributeSet attrs) {

        mContext = context;
        View view = View.inflate(context, R.layout.member_pay_result_info_brief, this); //layout inflate to this view

        tvProductName = (TextView) view.findViewById(R.id.tv_product_name);
        tvSn = (TextView) view.findViewById(R.id.tv_sn);
        tvDuration = (TextView) view.findViewById(R.id.tv_duration);
        tvDescription = (TextView) view.findViewById(R.id.tv_description);
    }

    public void setProductName(String s) {
        tvProductName.setText(s);
    }

    public void setSn(String s) {
        tvSn.setText(s);
    }

    public void setDuration(String s) {
        tvDuration.setText(s + mContext.getResources().getString(R.string.member_month_count));
    }

    public void setDescription(String s) {
        tvDescription.setText(s);
    }

    public void setPayResultInfo(String product, String sn, String duration, String description) {
        setProductName(product);
        setSn(sn);
        setDuration(duration);
        setDescription(description);
    }
}
