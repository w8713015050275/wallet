package com.letv.walletbiz.member.beans;

/**
 * Created by zhangzhiwei1 on 16-11-21.
 */

import com.letv.wallet.common.http.beans.LetvBaseBean;

public class BannerListBean implements LetvBaseBean {

    public BannerBean[] list;
    public long version;

    public static class BannerBean implements LetvBaseBean {

        public String memberType;
        public long banner_id;
        public String banner_name;
        public int position_id;
        public int rank;
        public int banner_type; //1为图片,2为链接， 3为应用
        public String banner_post;
        public String banner_link;
        public int need_token;
        public String jump_param;
        public String package_name;
        public long update_time;
    }
}
