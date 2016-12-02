package com.letv.walletbiz.main.bean;

import com.letv.wallet.common.http.beans.LetvBaseBean;

/**
 * Created by liuliang on 16-4-12.
 */
public class WalletBannerListBean implements LetvBaseBean {

    public WalletBannerBean[] list;

    public long version;

    public static class WalletBannerBean implements LetvBaseBean, Comparable<WalletBannerBean> {

        public static final int BANNER_TYPE_PIC = 1;
        public static final int BANNER_TYPE_LINK = 2;
        public static final int BANNER_TYPE_APP = 3;

        public long banner_id;

        public String banner_name;

        //Banner位置ID
        public int position_id;

        //Banner顺序位置
        public int rank;

        //Banner类型，1为图片,2为链接, 3为应用
        public int banner_type;

        //图片地址
        public String banner_post;

        //链接地址
        public String banner_link;

        //是否需要登陆：1-加token，0-不加token
        public int need_token;

        //跳转参数，当banner_type=3有效，否则为空字符串
        public String jump_param;

        //更新时间
        public long update_time;

        // 包名，当banner_type=3有效，否则为空字符串
        public String package_name;

        @Override
        public int compareTo(WalletBannerBean another) {
            return rank < another.rank ? -1 : 1;
        }
    }
}
