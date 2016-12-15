package com.letv.walletbiz.member.beans;

/**
 * Created by zhangzhiwei1 on 16-11-21.
 */

import com.letv.wallet.common.http.beans.LetvBaseBean;

public class CouponListBean implements LetvBaseBean {

    public CouponBean[] list;
    public int last_id;
    public int direction;
    public boolean next_more;
    public int limit;
}
