package com.letv.walletbiz.movie.beans;

import com.letv.wallet.common.http.beans.LetvBaseBean;

/**
 * Created by liuliang on 16-3-13.
 */
public class MoviePayResult implements LetvBaseBean {

    public static final int PAY_RESULT_UNPAY = 1;
    public static final int PAY_RESULT_PAID = 2;
    public static final int PAY_RESULT_REFUND = 3;
    public static final int PAY_RESULT_CANCEL = 4;

    //状态，1：未支付，2：已支付，3：已支付有退款，4：订单已取消
    public int status;

    //支付平台：1：支付宝，2：微信
    public int pay_platform;
}
