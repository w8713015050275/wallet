package com.letv.wallet.base.util;


import com.letv.tracker2.enums.EventType;
import com.letv.wallet.common.util.BaseAction;

import java.util.Map;


public class Action extends BaseAction {

    public static final String TAB_SET_PAGE_EXPOSE = "8.3";

    //Setting
    public static final String SETTING_SET_PWD_PAGE_EXPOSE = "8.3.3";
    public static final String SETTING_MOD_PWD_PAGE_EXPOSE = "8.3.5";

    // Pay
    public static final String PAY_PAGE_EXPOSE = "10.1";
    public static final String PAY_PAGE_PAY_CLICK = "10.1.1";
    public static final String PAY_PAGE_CLOSE = "10.1.2";
    public static final String PAY_PAGE_YOUΠ_ACTIVATED = "10.1.4";
    public static final String PAY_RESULT_EXCEPTION = "10.2";
    public static final String PAY_FAIL_EXPOSE = "10.1.5";

    /**
     * 账户相关
     **/
    //实名认证
    public static final String ACCOUNT_VERIFY_PAGE_EXPOSE = "8.3.1";
    public static final String ACCOUNT_VERIFY_PAGE_VERIFY = "8.3.2"; // 实名认证成功（自定义事件）
    public static final String ACCOUNT_VERIFY_PAGE_FAIL = "8.3.2.1"; // 实名认证失败时
    public static final String ACCOUNT_CREATE_FAIL= "8.3.7"; // 金融开户失败时
    //卡列表
    public static final String ACCOUNT_CARD_LIST_PAGE_EXPOSE = "11.3.4";
    public static final String ACCOUNT_CARD_LIST_ITEM_CLICK = "11.3.5";
    public static final String ACCOUNT_CARD_LIST_CARD_ADD = "11.3.6";
    //绑卡
    public static final String ACCOUNT_CARD_BIND_EXPOSE = "11.3.7";


    /**
     * Click
     *
     * @param widget
     */
    public static void uploadClick(String widget) {
        uploadClick(widget, null);
    }

    public static void uploadClick(final String widget, final Map<String, Object> props) {
        mUploadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                uploadCustomImpl(EventType.Click, widget, props);
            }
        });
    }

    /**
     * Expose
     *
     * @param widget
     */
    public static void uploadExpose(String widget) {
        uploadExpose(widget, null, null);
    }

    public static void uploadExpose(String widget, String from) {
        uploadExpose(widget, null, from, null);
    }
}
