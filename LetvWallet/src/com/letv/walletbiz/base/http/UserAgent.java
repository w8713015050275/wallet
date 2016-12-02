package com.letv.walletbiz.base.http;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.letv.wallet.common.BaseApplication;
import com.letv.wallet.common.util.LogHelper;
import com.letv.walletbiz.WalletApplication;

/**
 * Created by linquan on 15-12-31.
 */
public class UserAgent {
    /**
     * 读取ua
     *
     * @param context
     * @return
     */
    public static String ensureUserAgent(Context context) {
        return LeuiUserAgent.ensureUserAgent(context);
    }

    /**
     * 根据云平台生成user-agent, 用于问题跟踪
     *
     * @author fengzihua
     * @see http://jira.letv.cn/browse/LEUI-7730?filter=-1
     */
    static class LeuiUserAgent {
        // 相关key值
        public static final String TERMINAL_NAME = "Phone";
        public static final String BACK_SLASH = "/";
        public static final String SEMICOLON = ";";
        public static final String ONE_SPACE = " ";
        public static final String TERMINAL_CUSTOM_INFO = "active.v1";
        public static final String LEFT_PARENTTHESIS = "(";
        public static final String RIGHT_PARENTTHESIS = ")";

        // key值进行备份
        public static String USER_AGENT_VALUE = "";

        /**
         * Build user-agent for http request as the format <code><Product>;<Build_ID>;<package>[;interface_version]</code><br/>
         * See <code>http://wiki.letv.cn/pages/viewpage.action?pageId=46183570</code>
         *
         * @param context
         * @return
         */
        private static String ensureUserAgent(Context context) {
            if (TextUtils.isEmpty(USER_AGENT_VALUE)) {
                BaseApplication application = WalletApplication.getApplication();
                String defaultPackageName = application.getAppUA();

                StringBuilder sb = new StringBuilder();
                sb.append(TERMINAL_NAME)
                        .append(BACK_SLASH)
                        .append(Build.DEVICE)
                        .append(ONE_SPACE)
                        .append(LEFT_PARENTTHESIS)
                        .append(Build.ID)
                        .append(SEMICOLON)
                        .append(ONE_SPACE);

                sb.append(defaultPackageName);

                sb.append(SEMICOLON)
                        .append(ONE_SPACE)
                        .append(TERMINAL_CUSTOM_INFO)
                        .append(RIGHT_PARENTTHESIS);

                USER_AGENT_VALUE = sb.toString();
                LogHelper.d("[HttpHelper] ensureUserAgen: " + USER_AGENT_VALUE);
            }

            return USER_AGENT_VALUE;
        }

    }
}
