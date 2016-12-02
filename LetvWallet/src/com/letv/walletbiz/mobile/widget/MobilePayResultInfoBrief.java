package com.letv.walletbiz.mobile.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.letv.wallet.common.util.CommonConstants;
import com.letv.wallet.common.util.PhoneNumberUtils;
import com.letv.walletbiz.R;

/**
 * Created by linquan on 16-1-5.
 */
public class MobilePayResultInfoBrief extends LinearLayout {

    private TextView tvProductName;
    private TextView tvSn;
    private TextView tvNumber;

    public MobilePayResultInfoBrief(Context context) {
        this(context,null);
    }

    public MobilePayResultInfoBrief(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public MobilePayResultInfoBrief(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * 初始化函数
     */
    public void init(Context context, AttributeSet attrs) {


        View view = View.inflate(context, R.layout.mobile_pay_result_info_brief, this);

        tvProductName = (TextView) view.findViewById(R.id.tv_product_name);
        tvSn = (TextView) view.findViewById(R.id.tv_sn);
        tvNumber = (TextView) view.findViewById(R.id.tv_number);

    }

    public void setProductName(String s) {
        tvProductName.setText(s);
    }

    public void setSn(String s) {
        tvSn.setText(s);
    }

    public void setNumber(String s) {
        tvNumber.setText(PhoneNumberUtils.checkPhoneNumber(CommonConstants.PHONENUMBER_SIMPLE_REGEX, s, true));
    }

    public void setPayResultInfo(String product, String sn, String number) {
        setProductName(product);
        setSn(sn);
        setNumber(number);
    }
}
