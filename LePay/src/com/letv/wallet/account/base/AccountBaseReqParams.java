package com.letv.wallet.account.base;

import android.text.TextUtils;

import com.letv.wallet.PayApplication;
import com.letv.wallet.base.http.client.BaseRequestParams;
import com.letv.wallet.common.BaseApplication;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.DeviceUtils;
import com.letv.wallet.common.util.LogHelper;

import org.xutils.ex.HttpException;

/**
 * Created by lijunying on 17-1-3.
 */

public class AccountBaseReqParams extends BaseRequestParams {

    /**
     * 乐视网单点登录token
     * SSO_TK: string
     */
    static final String SSO_TK = "sso_tk";

    /**
     * IMEI
     * TERMINAL_ID: string
     */
    static final String TERMINAL_ID = "terminal_id";

    /**
     * 获取imei失败错误码
     * FAIL_REASON: string
     */
    static final String FAIL_REASON = "fail_reason";

    /**
     * 唯一请求编号，用于重试
     * REQUEST_NO: string(32)
     */
    static final String REQUEST_NO = "request_no";

    private static final int DEFAULT_RETRYCOUNT = 2;

    private int retryCount = DEFAULT_RETRYCOUNT;

    private static final int MAX_RETRYCOUNT = 5;


    protected AccountBaseReqParams(String path) {
        super(path);
        String imei = null;
        String failReason = null;
        //// TODO: 17-1-18 判断root , 虚拟机
        if (TextUtils.isEmpty(failReason)) {
            imei = DeviceUtils.getDeviceImei(BaseApplication.getApplication());
            failReason = TextUtils.isEmpty(imei) ? DeviceUtils.GET_DEVICE_ID_FAIL : null;
        }
        addBodyParameter(SSO_TK, AccountHelper.getInstance().getToken(PayApplication.getApplication()));
        addBodyParameter(TERMINAL_ID, imei);
        addBodyParameter(FAIL_REASON, failReason);
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount <= MAX_RETRYCOUNT ? retryCount : MAX_RETRYCOUNT;
    }

    public boolean permitsRetry(Throwable ex, int count) {
        if (ex instanceof HttpException) {
            HttpException httpEx = (HttpException) ex;
            int errorCode = httpEx.getCode();
            if (errorCode == 504) {
                if (count <= retryCount) {
                    return true;
                } else {
                    LogHelper.w("The Max Retry times has been reached!");
                }
            }
        }
        return false;
    }

}
