package com.letv.wallet.account.task;

import android.text.TextUtils;

import com.google.gson.reflect.TypeToken;
import com.letv.wallet.account.aidl.v1.AccountConstant;
import com.letv.wallet.account.aidl.v1.AccountInfo;
import com.letv.wallet.account.aidl.v1.IAccountCallback;
import com.letv.wallet.account.base.AccountGateway;
import com.letv.wallet.account.utils.AccountUtils;
import com.letv.wallet.common.http.beans.BaseResponse;
import com.letv.wallet.common.util.ParseHelper;
import com.letv.wallet.utils.SslUtil;

/**
 * Created by lijunying on 17-1-10.
 */

public class AccountQueryTask extends AccountBaseTask {
    private String mType ;

    private void initParams(String type) {
        this.mType = type;
    }

    public AccountQueryTask(String type, AccountCommonCallback<AccountInfo> callback) {
        super(callback);
        initParams(type);
    }

    public AccountQueryTask(String type, IAccountCallback callback) {
        super(callback);
        initParams(type);
    }

    @Override
    public BaseResponse onExecute() {
        BaseResponse<AccountInfo> response = AccountGateway.queryAccount(mType);
        if (response != null && response.errno == AccountConstant.RspCode.SUCCESS && response.data != null) {
            if (AccountConstant.QTYPE_ALL.equalsIgnoreCase(mType) || AccountConstant.QTYPE_CARD.equalsIgnoreCase(mType)  || AccountConstant.QTYPE_BASIC.equalsIgnoreCase(mType)) {
                decryptCard(response);
                updataBasicPreferences(response);

            }
        }
        return response;
    }

    // 如果查询包含卡信息， 解密 card 返回到 cardList
    private void decryptCard(BaseResponse<AccountInfo> response){
        if (!TextUtils.isEmpty(response.data.card)) {
            TypeToken typeToken = new TypeToken<AccountInfo.CardBin[]>() {};
            response.data.cardList =  ParseHelper.parseByGson(SslUtil.getInstance().decryptData(response.data.card), typeToken.getType());
        }
    }

    private void updataBasicPreferences(BaseResponse<AccountInfo> response){
        if (response.data.basic != null) {
            if (AccountConstant.BASIC_ACCOUNT_STATE_NON_ACTIVATED != response.data.basic.status) {
                AccountUtils.updateCreatedAccountStatus();
            }
            if (AccountConstant.BASIC_ACCOUNT_VERIFY_STATE_AUTHENTICATED.equalsIgnoreCase(response.data.basic.verifyStatus)) {
                AccountUtils.updateVerifyAccountStatus();
            }
        }
    }

    @Override
    public boolean checkParamsValidate() {
        if (TextUtils.isEmpty(mType)) {
            return false;
        }
        return true;
    }
}
