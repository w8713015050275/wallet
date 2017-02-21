package com.letv.wallet.account.aidl.v1;

/**
 * Created by lijunying on 16-12-26.
 */

public class AccountConstant {
    public static final String LEPAY_PKG = "com.letv.wallet";
    public static final String LEPAY_SERVICE_CLASS = "com.letv.wallet.account.service.AccountService";
    // aidl服务的Action
    public static final String ACTION_SERVICE_LEPAY = LEPAY_PKG + ".ACTION_SERVICE_LEPAYACCOUNT";

    public static final String SHAREDPREFERENCES_CREATE_ACCOUNT_SUFFIX = "_CA";
    public static final String SHAREDPREFERENCES_VERIFY_ACCOUNT_SUFFIX = "_VA";

    /** 账户查询类型 **/
    public static final String QTYPE_ALL = "all";
    public static final String QTYPE_BASIC = "basic";
    public static final String QTYPE_LELEHUA = "lelehua";
    public static final String QTYPE_CARD = "card";
    public static final String QTYPE_VIP = "vip";
    public static final String QTYPE_TIPS = "tips";

    /** H5跳转类型 **/
    public static final String JTYPE_SET_PAY_PWD = "set_pay_pwd";
    public static final String JTYPE_MOD_PAY_PWD = "mod_pay_pwd";
    public static final String JTYPE_RESET_PAY_PWD = "reset_pay_pwd";
    public static final String JTYPE_ADD_CARD = "add_card";
    public static final String JTYPE_CARD_LIST = "card_list";
    public static final String JTYPE_LELEHUA_NOACTIVE = "lelehua_noactive";
    public static final String JTYPE_LELEHUA_ACTIVE = "lelehua_active";
    public static final String JTYPE_LELEHUA_HOME = "lelehua_home";
    public static final String JTYPE_LELEHUA_BILL_LIST = "lelehua_bill_list";
    public static final String JTYPE_USER_GRADE = "user_grade";
    public static final String JTYPE_SSO_BIND_MOBILE = "sso_bind_mobile";

    /** 发送短信模板名称 **/
    public static final String SENDMSG_TEMP_APPLY_CERT = "APPLY_CERT";

    /** 基本账户 会员状态 **/
    public static final int BASIC_ACCOUNT_STATE_NON_ACTIVATED = 0;
    public static final int BASIC_ACCOUNT_STATE_NORMAL = 1;
    public static final int BASIC_ACCOUNT_STATE_FREEZING = 2;
    public static final int BASIC_ACCOUNT_STATE_CLOSED = 3;

    /** 基本账户 锁状态 **/
    public static final int BASIC_ACCOUNT_LOCK_STATE_UNLOCKED = 0;
    public static final int BASIC_ACCOUNT_LOCK_STATE_LOCKED = 1;

    /** 基本账户 密码状态 **/
    public static final String BASIC_ACCOUNT_PWD_STATE_SETTLED = "T";
    public static final String BASIC_ACCOUNT_PWD_STATE_UNSETTLED = "F";

    /** 基本账户 实名状态 **/
    public static final String BASIC_ACCOUNT_VERIFY_STATE_AUTHENTICATED = "T";
    public static final String BASIC_ACCOUNT_VERIFY_STATE_NON_AUTHENTICATED = "F";

    /** 乐乐花账户 激活状态
     *
     已激活，正常：0（点击跳转至乐乐花首页H5，jtype=lelehua_home）
     已激活，冻结：1（点击跳转至乐乐花首页H5，jtype=lelehua_home）
     未激活，不可再申请：2（点击跳转至乐乐花不可用H5，jtype=lelehua_noactive）
     未激活，可申请：3（点击跳转至乐乐花激活页面，jtype=lelehua_active）
     */
    public static final int LELEHUA_ACCOUNT_STATE_ACTIVATED= 0; //
    public static final int LELEHUA_ACCOUNT_STATE_ACTIVATED_FROZEN = 1;
    public static final int LELEHUA_ACCOUNT_STATE_NOACTIVATED_FROZEN= 2;
    public static final int LELEHUA_ACCOUNT_STATE_NOACTIVATED = 3;

    /** 银行卡 卡类型 **/
    public static final String CARD_BIN_TYPE_DEBIT = "DEBIT";
    public static final String CARD_BIN_TYPE_CREDIT = "CREDIT";
    public static final String CARD_BIN_TYPE_PASSBOOK = "PASSBOOK";
    public static final String CARD_BIN_TYPE_OTHER = "OTHER";

    /** 银行卡 卡属性 **/
    public static final String CARD_BIN_ATTRIBUTE_PRIVATE = "C";
    public static final String CARD_BIN_ATTRIBUTE_CORPORATE = "B";

    /** 银行卡 支付属性 **/
    public static final String CARD_PAY_ATTRIBUTE_CORPORATE = "qpay";

    /** 银行卡 卡状态 **/
    public static final int CARD_STATE_NORMAL = 1;
    public static final int CARD_STATE_LOCKED = 2;

    public static final String KEY_LEPAY_RESPONSE = "lePayResponse";
    public static final String EXTRA_AIDL_VERSION = "aidlVersion";



    /**  公共错误码 **/

    public static final class RspCode {
        public static final int SUCCESS = 10000;  // 成功
        public static final int ERRNO_PARAM = 10001; // 参数错误
        public static final int ERRNO_SIGN = 10002; // 签名错误
        public static final int ERRNO_USER = 10005; // 用户级别的错误，错误信息可展示给用户
        public static final int ERRNO_THIRD_API = 10006; // 第三方接口错误
        public static final int ERRNO_SYSTEM = 10007; // 系统错误
        public static final int ERRNO_USER_AUTH_FAILED = 10008; // 用户身份验证错误（比如sso_tk失效或者错误等）
        public static final int ERRNO_CURL_TIMEOUT = 20000; // 第三方接口超时

        /** 账户开户 错误号 **/
        public static final int ERRNO_MOBILE_EMPTY = 10100; // 手机号为空
        public static final int ERRNO_CREATE_ACCOUNT_FAILED = 10101; // 开户失败

        /** 账户信息查询 错误号 **/
        public static final int ERRNO_MEMBER_NOT_EXIST = 10105; // 会员不存在
        public static final int ERRNO_ACCOUNT_NOT_EXIST = 10106; // 账户不存在

        /** 实名认证 错误号 **/
        public static final int ERRNO_VERIFY_FAILED = 10103; // 实名认证失败
        public static final int ERRNO_MSG_CODE_FAILED = 10104; // 短信验证码错误

        /** 卡bin查询 错误号 **/
        public static final int ERRNO_BANK_CARD_ERRO = 10107; // 银行卡号错误

        /**  发送短信失败 错误码**/
        public static final int ERRNO_SEND_MSG_FAILED = 10108;

        /** 自定义错误妈 **/
        public static final int ERRNO_NO_NETWORK = -1; // 无网络
        public static final int ERROR_NETWORK = -2; // 网络错误
        public static final int ERROR_REMOTE_SERVICE_KILLED = -3; // service被意外终止
        public static final int ERROR_REMOTE_SERVICE_DISCONNECTE = -4; // service未连接

    }
}
