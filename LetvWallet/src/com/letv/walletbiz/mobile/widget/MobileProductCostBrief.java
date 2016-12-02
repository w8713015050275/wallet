package com.letv.walletbiz.mobile.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.letv.walletbiz.R;

/**
 * Created by changjiajie on 16-4-19.
 */
public class MobileProductCostBrief extends RelativeLayout {

    private MobileCostLabeledTextView v_total_price;
    private MobileCostLabeledTextView v_coupon_price;
    private MobileCostLabeledTextView v_pay_price;

    public static final int TAG_TOTAL = 1;
    public static final int TAG_COUPON = 2;
    public static final int TAG_PAY = 3;
    public static final int TAG_ALL = 4;

    public MobileProductCostBrief(Context context) {
        this(context, null);
    }

    public MobileProductCostBrief(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public MobileProductCostBrief(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * 初始化函数
     */
    public void init(Context context) {
        View view = View.inflate(context, R.layout.mobile_orderdetail_cost_brief, this);
        v_total_price = (MobileCostLabeledTextView) view.findViewById(R.id.tv_total_v);
        v_coupon_price = (MobileCostLabeledTextView) view.findViewById(R.id.tv_coupon_v);
        v_pay_price = (MobileCostLabeledTextView) view.findViewById(R.id.tv_pay_v);
        v_pay_price.setBottomLineShow(true);
    }

    public void setGONE(int[] tags) {
        int visible = View.GONE;
        for (int tag : tags) {
            switch (tag) {
                case TAG_ALL:
                    v_total_price.setVisibility(visible);
                    v_coupon_price.setVisibility(visible);
                    v_pay_price.setVisibility(visible);
                    break;
                case TAG_TOTAL:
                    v_total_price.setVisibility(visible);
                    break;
                case TAG_COUPON:
                    v_total_price.setVisibility(visible);
                    break;
                case TAG_PAY:
                    v_total_price.setVisibility(visible);
                    break;
            }
        }
    }

    public void setVisible(int[] tags) {
        int visible = View.VISIBLE;
        for (int tag : tags) {
            switch (tag) {
                case TAG_ALL:
                    v_total_price.setVisibility(visible);
                    v_coupon_price.setVisibility(visible);
                    v_pay_price.setVisibility(visible);
                    break;
                case TAG_TOTAL:
                    v_total_price.setVisibility(visible);
                    break;
                case TAG_COUPON:
                    v_total_price.setVisibility(visible);
                    break;
                case TAG_PAY:
                    v_total_price.setVisibility(visible);
                    break;
            }
        }
    }

    public void setVisible(int tag, int visible) {
        switch (tag) {
            case TAG_ALL:
                v_total_price.setVisibility(visible);
                v_coupon_price.setVisibility(visible);
                v_pay_price.setVisibility(visible);
                break;
            case TAG_TOTAL:
                v_total_price.setVisibility(visible);
                break;
            case TAG_COUPON:
                v_coupon_price.setVisibility(visible);
                break;
            case TAG_PAY:
                v_pay_price.setVisibility(visible);
                break;
        }
    }

    public void setPriceTopLineShow(int[] tags, boolean showTopLine) {
        for (int tag : tags) {
            switch (tag) {
                case TAG_ALL:
                    v_total_price.setTopLineShow(showTopLine);
                    v_coupon_price.setTopLineShow(showTopLine);
                    v_pay_price.setTopLineShow(showTopLine);
                    break;
                case TAG_TOTAL:
                    v_total_price.setTopLineShow(showTopLine);
                    break;
                case TAG_COUPON:
                    v_coupon_price.setTopLineShow(showTopLine);
                    break;
                case TAG_PAY:
                    v_pay_price.setTopLineShow(showTopLine);
                    break;
            }
        }
    }

    public void setPriceTopLineShow(int tag, boolean showTopLine) {
        switch (tag) {
            case TAG_ALL:
                v_total_price.setTopLineShow(showTopLine);
                v_coupon_price.setTopLineShow(showTopLine);
                v_pay_price.setTopLineShow(showTopLine);
                break;
            case TAG_TOTAL:
                v_total_price.setTopLineShow(showTopLine);
                break;
            case TAG_COUPON:
                v_coupon_price.setTopLineShow(showTopLine);
                break;
            case TAG_PAY:
                v_pay_price.setTopLineShow(showTopLine);
                break;
        }
    }

    public void setPriceBottomLineShow(int[] tags, boolean showBottomLine) {
        for (int tag : tags) {
            switch (tag) {
                case TAG_ALL:
                    v_total_price.setBottomLineShow(showBottomLine);
                    v_coupon_price.setBottomLineShow(showBottomLine);
                    v_pay_price.setBottomLineShow(showBottomLine);
                    break;
                case TAG_TOTAL:
                    v_total_price.setBottomLineShow(showBottomLine);
                    break;
                case TAG_COUPON:
                    v_coupon_price.setBottomLineShow(showBottomLine);
                    break;
                case TAG_PAY:
                    v_pay_price.setBottomLineShow(showBottomLine);
                    break;
            }
        }
    }

    public void setPriceBottomLineShow(int tag, boolean showBottomLine) {
        switch (tag) {
            case TAG_ALL:
                v_total_price.setBottomLineShow(showBottomLine);
                v_coupon_price.setBottomLineShow(showBottomLine);
                v_pay_price.setBottomLineShow(showBottomLine);
                break;
            case TAG_TOTAL:
                v_total_price.setBottomLineShow(showBottomLine);
                break;
            case TAG_COUPON:
                v_coupon_price.setBottomLineShow(showBottomLine);
                break;
            case TAG_PAY:
                v_pay_price.setBottomLineShow(showBottomLine);
                break;
        }
    }

    public void setTotalPrice(String price) {
        setTotalPrice(price, MobileCostLabeledTextView.BLACKCOLOR);
    }

    public void setTotalPrice(String price, int unitColor) {
        v_total_price.setTextPrice(price);
        v_total_price.setCostInfoColor(unitColor);
    }

    public void setCouponContent(String content) {
        setCouponContent(content, MobileCostLabeledTextView.BLACKCOLOR);
    }

    public void setCouponContent(String content, int unitColor) {
        v_coupon_price.setTextContent(content);
        v_coupon_price.setCostInfoColor(unitColor);
    }

    public void setCouponPrice(Context context, float price) {
        setCouponPrice(context, price, MobileCostLabeledTextView.BLACKCOLOR);
    }

    public void setCouponPrice(Context context, float price, int unitColor) {
        if (price > 0.0F) {
            v_coupon_price.setTextPrice(String.format(context.getString(R.string.mobile_order_desc_coupon_price), price));
        } else {
            v_coupon_price.setTextPrice(String.valueOf(price));
        }
        v_coupon_price.setCostInfoColor(unitColor);
    }

    public void setCouponOnclickListener(OnClickListener lis) {
        v_coupon_price.setOnclickListener(lis);
    }

    public void setPayPrice(String price) {
        setPayPrice(price, MobileCostLabeledTextView.BLACKCOLOR);
    }

    public void setPayPrice(String price, int unitColor) {
        v_pay_price.setTextPrice(price);
        v_pay_price.setCostInfoColor(unitColor);
    }
}
