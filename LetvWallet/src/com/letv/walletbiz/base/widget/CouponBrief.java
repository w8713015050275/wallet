package com.letv.walletbiz.base.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.letv.walletbiz.R;

/**
 * Created by changjiajie on 16-8-30.
 */
public class CouponBrief extends RelativeLayout {

    private TextView tv_coupon_content;
    private ImageView iv_jump;

    public CouponBrief(Context context) {
        this(context, null);
    }

    public CouponBrief(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CouponBrief(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        View view = View.inflate(context, R.layout.coupon_brief, this);
        iv_jump = (ImageView) view.findViewById(R.id.iv_jump);
        tv_coupon_content = (TextView) view.findViewById(R.id.tv_coupon_content);
    }

    public void setContent(String content) {
        if (tv_coupon_content == null) return;
        tv_coupon_content.setText(content);
    }

    public void setClickListener(OnClickListener lis) {
        if (tv_coupon_content == null || iv_jump == null) return;
        tv_coupon_content.setOnClickListener(lis);
        iv_jump.setOnClickListener(lis);
    }
}
