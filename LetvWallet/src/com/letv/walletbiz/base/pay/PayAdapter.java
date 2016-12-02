package com.letv.walletbiz.base.pay;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by linquan on 16-1-28.
 */
public interface PayAdapter extends BasePayAdapter {
    int getTitle();

    String getCost();

    //Todo time counter
    int getTimeLimitation();

    View createContentView(Context context, ViewGroup parent);

    View createFootNotedView(Context context, ViewGroup parent);

}