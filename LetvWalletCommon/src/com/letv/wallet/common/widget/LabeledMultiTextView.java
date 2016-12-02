package com.letv.wallet.common.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.letv.wallet.common.R;

/**
 * Created by linquan on 16-4-19.
 */
public class LabeledMultiTextView extends LabeledTextView {
    public LabeledMultiTextView(Context context) {
        super(context);
    }

    public LabeledMultiTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public LabeledMultiTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View inflate(Context context) {
        return View.inflate(context, R.layout.labeled_multi_text_item, this);
    }

    @Override
    public void setIconShow() {
    }
}