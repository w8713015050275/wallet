package com.letv.walletbiz.mobile.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.letv.walletbiz.R;
import com.letv.walletbiz.base.util.StringUtils;

/**
 * Created by changjiajie on 16-4-19.
 */
public class MobileCostLabeledTextView extends RelativeLayout {

    public static int BLUECOLOR;
    public static int BLACKCOLOR;
    public static int REDCOLOR;
    private int unitColor;
    private ImageView iv_jump;
    private LinearLayout ll_cost_action;
    private TextView tv_label_cost;
    private TextView tv_cost;
    private View top_line;
    private View bottom_line;

    private String label_string;


    public MobileCostLabeledTextView(Context context) {
        this(context, null);
    }

    public MobileCostLabeledTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public MobileCostLabeledTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CommonLabeledTextView);
        label_string = typedArray.getString(R.styleable.CommonLabeledTextView_label_text);
        BLUECOLOR = getResources().getColor(R.color.colorAccent);
        BLACKCOLOR = getResources().getColor(R.color.mobile_order_desc_black_color);
        REDCOLOR = getResources().getColor(R.color.red);
        unitColor = BLACKCOLOR;
        View view = View.inflate(context, R.layout.mobile_cost_labeled_text_item, this);
        if (attrs == null) {
            return;
        }
        ll_cost_action = (LinearLayout) view.findViewById(R.id.ll_cost_action);
        iv_jump = (ImageView) view.findViewById(R.id.iv_jump);
        iv_jump.setVisibility(View.GONE);
        tv_label_cost = (TextView) view.findViewById(R.id.tv_label_cost);
        tv_label_cost.setText(label_string);
        tv_cost = (TextView) view.findViewById(R.id.tv_cost);
        top_line = (View) view.findViewById(R.id.top_line);
        bottom_line = (View) view.findViewById(R.id.bottom_line);
    }

    public void setBgColor(int colorId) {
        setBackgroundResource(colorId);
    }

    public void setTextTitle(String s) {
        tv_label_cost.setText(s);
    }

    public void setTextContent(String content) {
        tv_cost.setText(StringUtils.getPriceUnit(getContext(), content));
    }

    public void setTextContentColor(int color) {
        tv_cost.setTextColor(color);
    }

    public void setTextLabelSize(int size) {
        if (tv_label_cost == null) return;
        tv_label_cost.setTextSize(size);
    }

    public void setTextContentSize(int size) {
        if (tv_cost == null) return;
        tv_cost.setTextSize(size);
    }

    public void setTextPrice(String s) {
        tv_cost.setText(StringUtils.getPriceUnit(getContext(), s));
    }

    public TextView getLabelCostTv() {
        return tv_label_cost;
    }

    public void setCostInfoColor() {
        tv_cost.setTextColor(unitColor);
    }

    public void setCostInfoColor(int colorId) {
        unitColor = colorId;
        setCostInfoColor();
    }

    public TextView getCostTv() {
        return tv_cost;
    }

    public ImageView getJumpIv() {
        return iv_jump;
    }

    public void setIconShow() {
        iv_jump.setVisibility(View.VISIBLE);
    }

    public void setTopLineShow(boolean b) {
        top_line.setVisibility(b ? View.VISIBLE : View.GONE);
    }

    public void setBottomLineShow(boolean b) {
        bottom_line.setVisibility(b ? View.VISIBLE : View.GONE);
    }

    public void setOnclickListener(OnClickListener lis) {
        setIconShow();
        ll_cost_action.setOnClickListener(lis);
    }

}
