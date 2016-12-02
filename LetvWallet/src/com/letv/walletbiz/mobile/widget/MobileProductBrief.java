package com.letv.walletbiz.mobile.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.letv.wallet.common.util.CommonConstants;
import com.letv.wallet.common.util.PhoneNumberUtils;
import com.letv.wallet.common.widget.LabeledTextView;
import com.letv.walletbiz.R;

/**
 * Created by linquan on 16-1-5.
 */
public class MobileProductBrief extends LinearLayout {

    private LabeledTextView v_Line1;
    private LabeledTextView v_Line2;
    private LabeledTextView v_Line3;

    public MobileProductBrief(Context context) {
        this(context, null);
    }

    public MobileProductBrief(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public MobileProductBrief(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * 初始化函数
     */
    public void init(Context context, AttributeSet attrs) {


        View view = View.inflate(context, R.layout.mobile_order_product_brief, this);

        v_Line1 = (LabeledTextView) view.findViewById(R.id.tv_product_name);
        v_Line2 = (LabeledTextView) view.findViewById(R.id.tv_sn);
        v_Line3 = (LabeledTextView) view.findViewById(R.id.tv_number);

    }

    public void setLine1(String s) {
        v_Line1.setTextSummery(s);
    }

    public void setLine2(String s) {
        v_Line2.setTextSummery(s);
    }

    public void setLine3(String s) {
        v_Line3.setTextSummery(PhoneNumberUtils.checkPhoneNumber(CommonConstants.PHONENUMBER_SIMPLE_REGEX, s, true));
    }

    public void setAllLines(String line1, String line2, String line3) {
        setLine1(line1);
        setLine2(line2);
        setLine3(line3);
    }

    public void setAllLines(String line1, String line3) {
        v_Line2.setVisibility(View.GONE);
        v_Line1.setBottomLineShow(false);
        v_Line3.setBottomLineShow(false);
        setLine1(line1);
        setLine3(line3);
    }
}
