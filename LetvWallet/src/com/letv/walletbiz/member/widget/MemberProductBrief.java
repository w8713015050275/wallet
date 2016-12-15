package com.letv.walletbiz.member.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.letv.wallet.common.widget.LabeledTextView;
import com.letv.walletbiz.R;

/**
 * Created by zhanghuancheng on 16-11-25.
 */
public class MemberProductBrief extends LinearLayout {
    private LabeledTextView v_Line1;
    private LabeledTextView v_Line2;
    private LabeledTextView v_Line3;
    private LabeledTextView v_Line4;

    public MemberProductBrief(Context context) {
        this(context, null);
    }

    public MemberProductBrief(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MemberProductBrief(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public void init(Context context, AttributeSet attrs) {


        View view = View.inflate(context, R.layout.member_order_product_brief, this);

        v_Line1 = (LabeledTextView) view.findViewById(R.id.tv_product_name);
        v_Line2 = (LabeledTextView) view.findViewById(R.id.tv_sn);
        v_Line3 = (LabeledTextView) view.findViewById(R.id.tv_duration);
        v_Line4 = (LabeledTextView) view.findViewById(R.id.tv_description);

    }

    public void setLine1(String s) {
        v_Line1.setTextSummery(s);
    }

    public void setLine2(String s) {
        v_Line2.setTextSummery(s);
    }

    public void setLine3(String s) {
        v_Line3.setTextSummery(s);
    }

    public void setLine4(String s) {
        v_Line4.setTextSummery(s);
    }

    /**
     *
     * @param line1 商品名称
     * @param line2 订单编号
     * @param line3 会员时长
     * @param line4 会员描述
     */
    public void setAllLines(String line1, String line2, String line3, String line4) {
        setLine1(line1);
        setLine2(line2);
        setLine3(line3);
        setLine4(line4);
    }

    public void setAllLines(String line1, String line3) {
        v_Line2.setVisibility(View.GONE);
        v_Line1.setBottomLineShow(false);
        v_Line3.setBottomLineShow(false);
        setLine1(line1);
        setLine3(line3);
    }

    public void setBgColor(int colorId) {
        setBackgroundResource(colorId);
    }
}
