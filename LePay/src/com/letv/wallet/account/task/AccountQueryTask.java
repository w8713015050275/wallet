package com.letv.wallet.account.task;

import android.text.TextUtils;

import com.google.gson.reflect.TypeToken;
import com.letv.wallet.account.aidl.v1.AccountConstant;
import com.letv.wallet.account.aidl.v1.AccountInfo;
import com.letv.wallet.account.aidl.v1.IAccountCallback;
import com.letv.wallet.account.base.AccountGateway;
import com.letv.wallet.account.utils.AccountUtils;
import com.letv.wallet.common.http.beans.BaseResponse;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.DigestUtils;
import com.letv.wallet.common.util.ParseHelper;
import com.letv.wallet.common.util.SharedPreferencesHelper;
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
            if (mType == AccountConstant.QTYPE_ALL || mType == AccountConstant.QTYPE_CARD  || mType == AccountConstant.QTYPE_BASIC) {
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
        if (AccountUtils.hasCreatedAccount() && AccountUtils.hasVerifyAccount()) {
            return; // 本地已缓存返回
        }
        if (response.data.basic != null) {
            String uid = AccountHelper.getInstance().getUid();
            if (AccountConstant.BASIC_ACCOUNT_STATE_NON_ACTIVATED != response.data.basic.status) { // 更新缓存开户状态
                updateCreatePreferences(uid);
            }
            if (AccountConstant.BASIC_ACCOUNT_VERIFY_STATE_AUTHENTICATED.equalsIgnoreCase(response.data.basic.verifyStatus)) { // 更新缓存认证状态
                updateVerifyPreferences(uid);
            }
        }
    }

    private void updateCreatePreferences(String uid){
        SharedPreferencesHelper.putBoolean(DigestUtils.getMd5_30(uid+AccountConstant.SHAREDPREFERENCES_CREATE_ACCOUNT_SUFFIX), true);
    }

    private void updateVerifyPreferences(String uid){
        SharedPreferencesHelper.putBoolean(DigestUtils.getMd5_30(uid+AccountConstant.SHAREDPREFERENCES_VERIFY_ACCOUNT_SUFFIX), true);
    }

    @Override
    public boolean checkParamsValidate() {
        if (TextUtils.isEmpty(mType)) {
            return false;
        }
        return true;
    }
}
