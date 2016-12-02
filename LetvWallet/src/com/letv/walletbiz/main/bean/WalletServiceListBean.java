package com.letv.walletbiz.main.bean;

import com.letv.wallet.common.http.beans.LetvBaseBean;

/**
 * Created by liuliang on 16-4-11.
 */
public class WalletServiceListBean implements LetvBaseBean {

    public WalletServiceBean[] list;

    //数据版本 最新更新时间
    public long version;

    public static class WalletServiceBean implements LetvBaseBean, Comparable<WalletServiceBean> {

        public static final int JUMP_TYPE_APP = 1;
        public static final int JUMP_TYPE_WEB = 2;

        public static final int STATE_ONLING = 1;
        public static final int STATE_WILL_ONLING = 2;
        public static final int STATE_OFFLINE = 3;
        public static final int STATE_DELETE = 4;

        //服务id
        public int service_id;

        //服务名称
        public String service_name;

        //icon图标
        public String icon;

        //跳转类型：1-APP跳转，2-网页跳转
        public int jump_type;

        //跳转链接，当jump_type=2时候有效，否则为空字符串
        public String jump_link;

        //是否需要登陆：1-加token，0-不加token
        public int need_token;

        //跳转参数，当jump_type=1有效，否则为空字符串
        public String jump_param;

        //包名，当jump_type=1有效，否则为空字符串
        public String package_name;

        //状态：1-在线，2-待上线，3-下线，4-已删除（客户端只能获取到在线状态的数据）
        public int state;

        //排序值，值越小，越靠前
        public int rank;

        //更新时间
        public long update_time;

        @Override
        public int compareTo(WalletServiceBean another) {
            return rank < another.rank ? -1 : 1;
        }
    }
}
