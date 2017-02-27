package com.letv.wallet.online.bean;

import com.letv.wallet.common.http.beans.LetvBaseBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by changjiajie on 17-1-9.
 */

public class LePayChannelListBean implements LetvBaseBean {

    protected String order_no;
    protected String price;
    protected List<LePayChannelBean> channels;

    public String getOrderNo() {
        return order_no;
    }

    public String getPrice() {
        return price;
    }

    public List<LePayChannelBean> getChannels() {
        return channels;
    }
}
