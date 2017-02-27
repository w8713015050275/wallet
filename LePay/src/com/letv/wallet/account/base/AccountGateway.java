package com.letv.wallet.account.base;

import com.google.gson.reflect.TypeToken;
import com.letv.wallet.account.aidl.v1.AccountInfo;
import com.letv.wallet.account.aidl.v1.CardbinAvailableInfo;
import com.letv.wallet.account.aidl.v1.RedirectURL;
import com.letv.wallet.account.utils.RequestUtils;
import com.letv.wallet.common.http.beans.BaseResponse;
import com.letv.wallet.online.LePayConstants;
import com.letv.wallet.online.activity.LePayEntryActivity;
import com.letv.wallet.online.bean.LePayCashierUrlBean;
import com.letv.wallet.online.bean.LePayChannelListBean;
import com.letv.wallet.online.bean.LePayOrderStatusBean;
import com.letv.wallet.utils.SslUtil;

import org.xutils.xmain;

import java.lang.reflect.Type;

/**
 * Created by lijunying on 17-1-18.
 */

public class AccountGateway {

    /**
     * 账户相关
     */
    static final String ACCOUNT_BASE_PATH = "account/";

    /**
     * 账户开户
     */
    static final String ACCOUNT_CREATE = ACCOUNT_BASE_PATH + "api/v1/create";

    /**
     * 账户信息查询
     */
    static final String ACCOUNT_QUERY = ACCOUNT_BASE_PATH + "api/v1/query";

    /**
     * 实名认证
     */
    static final String ACCOUNT_VERIFY = ACCOUNT_BASE_PATH + "api/v1/verify";

    /**
     * 发送短信
     */
    static final String ACCOUNT_SENDMSG = ACCOUNT_BASE_PATH + "api/v1/sendmsg";

    /**
     * 统一跳转H5
     */
    static final String ACCOUNT_REDIRECT = ACCOUNT_BASE_PATH + "api/v1/redirect";


    /**
     * 银行相关
     */
    static final String BANK_BASE_PATH = "bank/";

    /**
     * 卡bin查询
     */
    static final String BANK_CARDBIN = BANK_BASE_PATH + "api/v1/cardbin";


    static final String QTYPE = "qtype";

    static final String JTYPE = "jtype";

    static final String KEY_ACCOUNT_NAME = "account_name";
    static final String KEY_IDENTITY_NUM = "identity_num";
    static final String KEY_BANK_NO = "bank_no";
    static final String KEY_MOBILE = "mobile";
    static final String KEY_MSG_CODE = "msg_code";

    static final String KEY_TEMPLATE = "template";

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    public static <T> T postSync(AccountBaseReqParams entity, Type resultType) {
        if (entity != null) {
            int retryCount = 0;
            boolean retry = true;
            while (retry) {
                retry = false;
                try {
                    String requestNo = RequestUtils.getRequestNo(entity.getUri(), entity.getStringParams());
                    entity.addBodyParameter(AccountBaseReqParams.REQUEST_NO, requestNo);
                    return xmain.http().postSync(entity, resultType);
                } catch (Throwable ex) {
                    retry = entity.permitsRetry(ex, ++retryCount);
                    ex.printStackTrace();
                }
            }
        }
        return null;
    }

    static AccountBaseReqParams buildBaseParams(String path) {
        return new AccountBaseReqParams(path);
    }

    /**
     * 账户开户
     */
    public static BaseResponse createAcount() {
        return postSync(buildBaseParams(ACCOUNT_CREATE), new TypeToken<BaseResponse<String>>() {
        }.getType());
    }

    /**
     * 账户信息查询
     */
    public static BaseResponse<AccountInfo> queryAccount(String type) {
        AccountBaseReqParams params = buildBaseParams(ACCOUNT_QUERY);
        params.addBodyParameter(QTYPE, type);
        return postSync(params, new TypeToken<BaseResponse<AccountInfo>>() {
        }.getType());
    }

    /**
     * 实名认证
     */
    public static BaseResponse verifyAccount(String accountName, String identityNum, String bankNo, String mobile, String msgCode) {
        AccountBaseReqParams params = buildBaseParams(ACCOUNT_VERIFY);
        SslUtil sslUtil = SslUtil.getInstance();
        params.addBodyParameter(KEY_ACCOUNT_NAME, sslUtil.encryptData(accountName));
        params.addBodyParameter(KEY_IDENTITY_NUM, sslUtil.encryptData(identityNum));
        params.addBodyParameter(KEY_BANK_NO, sslUtil.encryptData(bankNo));
        params.addBodyParameter(KEY_MOBILE, sslUtil.encryptData(mobile));
        params.addBodyParameter(KEY_MSG_CODE, msgCode);
        return postSync(params, new TypeToken<BaseResponse<String>>() {
        }.getType());
    }

    /**
     * 统一跳转H5
     */
    public static BaseResponse<RedirectURL> redirect(String jumpType) {
        AccountBaseReqParams params = buildBaseParams(ACCOUNT_REDIRECT);
        params.addBodyParameter(JTYPE, jumpType);
        return postSync(params, new TypeToken<BaseResponse<RedirectURL>>() {
        }.getType());
    }

    /**
     * 卡bin查询接口
     */
    public static BaseResponse<CardbinAvailableInfo> availableCarbin(String bankNo) {
        AccountBaseReqParams params = buildBaseParams(AccountGateway.BANK_CARDBIN);
        params.addBodyParameter(KEY_BANK_NO, SslUtil.getInstance().encryptData(bankNo));
        return postSync(params, new TypeToken<BaseResponse<CardbinAvailableInfo>>() {
        }.getType());
    }

    /**
     * 发送短信
     */
    public static BaseResponse sendMsg(String mobile, String template) {
        AccountBaseReqParams params = buildBaseParams(AccountGateway.ACCOUNT_SENDMSG);
        params.addBodyParameter(KEY_MOBILE, SslUtil.getInstance().encryptData(mobile));
        params.addBodyParameter(KEY_TEMPLATE, template);
        return postSync(params, new TypeToken<BaseResponse<String>>() {
        }.getType());
    }


    /**
     * 请求支付列表
     */
    public static BaseResponse payChannel(String payInfo) {
        AccountBaseReqParams reqParams = new AccountBaseReqParams(LePayConstants.PATH.PAY_CHANNEL);
        reqParams.addBodyParameter(LePayConstants.PARAM.PAY_INFO, payInfo);
        return postSync(reqParams, new TypeToken<BaseResponse<LePayChannelListBean>>() {
        }.getType());
    }

    /**
     * 请求支付地址
     */
    public static BaseResponse showCashier(String payInfo, String payMethod) {
        AccountBaseReqParams reqParams = buildBaseParams(LePayConstants.PATH.GET_CASHIER_URL);
        reqParams.addBodyParameter(LePayConstants.PARAM.PAY_INFO, payInfo);
        reqParams.addBodyParameter(LePayConstants.PARAM.PAY_METHOD, payMethod);
        return postSync(reqParams, new TypeToken<BaseResponse<LePayCashierUrlBean>>() {
        }.getType());
    }

    /**
     * 获取订单状态
     */
    public static BaseResponse orderStatus(String orderNo) {
        AccountBaseReqParams reqParams = buildBaseParams(LePayConstants.PATH.ORDER_NO);
        reqParams.addBodyParameter(LePayConstants.PARAM.ORDER_NO, orderNo);
        return postSync(reqParams, new TypeToken<BaseResponse<LePayOrderStatusBean>>() {
        }.getType());
    }

}
