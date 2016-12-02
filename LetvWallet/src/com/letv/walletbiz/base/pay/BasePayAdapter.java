package com.letv.walletbiz.base.pay;

import android.content.Context;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by linquan on 16-1-28.
 */
public interface BasePayAdapter extends Serializable {
    //For H5 PAY
    String getPrepayInfo(Context context, int platform);


    String getPrepayInfo(Context context);

    String getPrepayInfoPath();

    HashMap<String, String> getQueryParamMap();

    HashMap<String, String> getBodyParamMap();

    boolean addToken();



    /*
    Return ：   true - show payResultActivity ; false -won't show
      */
    boolean onPayResult(int state);
}