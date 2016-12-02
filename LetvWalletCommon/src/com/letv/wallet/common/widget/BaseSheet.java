package com.letv.wallet.common.widget;

import android.content.Context;
import android.content.res.Resources;

import com.letv.shared.widget.LeBottomSheet;
import com.letv.wallet.common.view.OnBaseConfirmListener;

/**
 * Created by linquan on 16-1-6.
 */
public abstract class BaseSheet extends LeBottomSheet {

    Resources res;
    protected Context mContext;
    protected OnBaseConfirmListener onCancelListener;

    public BaseSheet(Context context, boolean slideFromTop) {
        super(context, slideFromTop);
        init(context);
    }

    public BaseSheet(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        res = context.getResources();
        this.mContext = context;
    }

    protected void doClose() {
        if (BaseSheet.this.isShowing())
            BaseSheet.this.disappear();
    }
}

