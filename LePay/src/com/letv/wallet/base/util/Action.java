package com.letv.wallet.base.util;


import com.letv.tracker.enums.EventType;
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
        uploadExpose(widget, null);
    }

    public static void uploadExpose(String widget, String from) {
        uploadExpose(widget, null, from, null);
    }
}
