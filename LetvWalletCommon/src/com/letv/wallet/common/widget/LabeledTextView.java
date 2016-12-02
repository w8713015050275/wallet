package com.letv.wallet.common.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.letv.wallet.common.R;

/**
 * Created by linquan on 16-1-5.
 */
public class LabeledTextView extends RelativeLayout {
    private ImageView iv_jump;
    private TextView tv_label;
    private TextView tv_content;
    private View top_line;
    private View bottom_line;

    private String big_string;
    private String little_string;

    public LabeledTextView(Context context) {
        super(context);
        init(context, null);
    }

    public LabeledTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public LabeledTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    /**
     * 初始化函数
     */
    public void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CommonLabeledTextView);
        big_string = typedArray.getString(R.styleable.CommonLabeledTextView_label_text);
        little_string = typedArray.getString(R.styleable.CommonLabeledTextView_content_text);
        typedArray.recycle();

        View view = inflate(context);

        iv_jump = (ImageView) view.findViewById(R.id.iv_jump);
        if (iv_jump != null)
            iv_jump.setVisibility(View.GONE);
        tv_label = (TextView) view.findViewById(R.id.tv_label);
        tv_label.setText(big_string);
        tv_content = (TextView) view.findViewById(R.id.tv_content);
        tv_content.setText(little_string);
        top_line = (View) view.findViewById(R.id.top_line);
        bottom_line= (View) view.findViewById(R.id.bottom_line);
        if (TextUtils.isEmpty(little_string)) {
            if (iv_jump != null)
                iv_jump.setVisibility(View.VISIBLE);
        }
    }

    protected View inflate(Context context){
        return View.inflate(context, R.layout.labeled_text_item, this);
    }
    public void setTextTitle(String s) {
        tv_label.setText(s);
    }

    public void setTextSummery(String s) {
        if (iv_jump != null)
            iv_jump.setVisibility(View.GONE);
        tv_content.setText(s);
    }

    public void setIconShow() {
        tv_content.setText("");
        iv_jump.setVisibility(View.VISIBLE);
    }

    public void setTopLineShow(boolean b) {
        top_line.setVisibility(b ? View.VISIBLE : View.GONE);
    }
    public void setBottomLineShow(boolean b) {
        bottom_line.setVisibility(b ? View.VISIBLE : View.GONE);
    }

    public TextView getSummeryView(){
        return tv_content;
    }


}
