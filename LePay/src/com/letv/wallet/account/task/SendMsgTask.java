package com.letv.wallet.account.task;

import android.text.TextUtils;

import com.letv.wallet.account.aidl.v1.IAccountCallback;
import com.letv.wallet.account.base.AccountGateway;
import com.letv.wallet.common.http.beans.BaseResponse;

/**
 *  Created by lijunying on 16-12-26.
 */

public class SendMsgTask extends AccountBaseTask {
    private String mobile , template;

    public SendMsgTask(String mobile , String template, AccountCommonCallback callback) {
        super(callback);
        this.mobile = mobile;
        this.template = template;
    }

    public SendMsgTask(String mobile, String template, IAccountCallback callback) {
        super(callback);
        this.mobile = mobile;
        this.template = template;
    }

    @Override
    public BaseResponse onExecute() {
         return AccountGateway.sendMsg(mobile, template);
    }

    @Override
    public boolean checkParamsValidate() {
        if (TextUtils.isEmpty(mobile) || TextUtils.isEmpty(template)) {
            return false;
        }
        return true;
    }
}
